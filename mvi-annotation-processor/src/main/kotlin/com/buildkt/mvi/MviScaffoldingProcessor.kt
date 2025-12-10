package com.buildkt.mvi

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.Locale

class MviScaffoldingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    // Helper data class to hold nav argument info
    private data class NavArgumentInfo(
        val name: String,
        val typeName: TypeName,
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val paneSymbols = resolver.getSymbolsWithAnnotation(MviScreen::class.qualifiedName!!)
        val (validPaneSymbols, invalidPaneSymbols) = paneSymbols.partition { it.validate() }

        val sideEffectIntents = resolver.getSymbolsWithAnnotation(TriggersSideEffect::class.qualifiedName!!)
        val (validSideEffectIntentSymbols, invalidSideEffectIntentSymbols) = sideEffectIntents.partition { it.validate() }

        validPaneSymbols
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { paneFunction ->
                try {
                    val (uiStateDeclaration, intentDeclaration) = getMviDeclarationsFromAnnotation(paneFunction)

                    validatePaneFunction(paneFunction, uiStateDeclaration, intentDeclaration)

                    val relevantIntents =
                        validSideEffectIntentSymbols
                            .filterIsInstance<KSClassDeclaration>()
                            .filter { intent -> intentDeclaration.asStarProjectedType().isAssignableFrom(intent.asStarProjectedType()) }

                    val navArguments = findNavArguments(paneFunction)
                    val sideEffectMapClassName =
                        generateSideEffectMapClass(paneFunction, uiStateDeclaration, intentDeclaration, relevantIntents)
                    val sideEffectBuilderClassName =
                        generateSideEffectBuilderClass(paneFunction, uiStateDeclaration, intentDeclaration, relevantIntents)
                    val configClassName =
                        generateConfigClass(
                            paneFunction,
                            uiStateDeclaration,
                            intentDeclaration,
                            sideEffectBuilderClassName,
                            sideEffectMapClassName,
                            relevantIntents,
                        )
                    val (viewModelClassName, factoryClassName) =
                        generateViewModel(
                            paneFunction,
                            uiStateDeclaration,
                            intentDeclaration,
                            sideEffectMapClassName,
                            configClassName,
                        )
                    generateNavigationForPane(paneFunction, viewModelClassName, factoryClassName, configClassName, navArguments)
                } catch (e: Exception) {
                    logger.error(
                        "Failed to process @MviScreen on ${paneFunction.simpleName.asString()}: ${e.stackTraceToString()}",
                        paneFunction,
                    )
                }
            }

        return invalidPaneSymbols + invalidSideEffectIntentSymbols
    }

    private fun findNavArguments(paneFunction: KSFunctionDeclaration): List<NavArgumentInfo> {
        val navArgumentAnnotationName = NavArgument::class.qualifiedName!!

        return paneFunction.parameters
            .filter { param ->
                param.annotations.any {
                    it.annotationType
                        .resolve()
                        .declaration.qualifiedName
                        ?.asString() ==
                        navArgumentAnnotationName
                }
            }.map { param ->
                NavArgumentInfo(
                    name = param.name!!.asString(),
                    typeName = param.type.toTypeName(),
                )
            }.toList()
    }

    private fun getMviDeclarationsFromAnnotation(paneFunction: KSFunctionDeclaration): Pair<KSClassDeclaration, KSClassDeclaration> {
        val annotation = paneFunction.annotations.first { it.shortName.asString() == MviScreen::class.simpleName }
        val uiStateArgument = annotation.arguments.first { it.name?.asString() == "uiState" }
        val intentArgument = annotation.arguments.first { it.name?.asString() == "intent" }

        val uiStateType = uiStateArgument.value as KSType
        val intentType = intentArgument.value as KSType

        return Pair(
            uiStateType.declaration as KSClassDeclaration,
            intentType.declaration as KSClassDeclaration,
        )
    }

    private fun validatePaneFunction(
        paneFunction: KSFunctionDeclaration,
        uiStateDeclaration: KSClassDeclaration,
        intentDeclaration: KSClassDeclaration,
    ) {
        val functionName = paneFunction.simpleName.asString()
        val parameters = paneFunction.parameters.associateBy { it.name!!.asString() }

        val expectedStateTypeName = uiStateDeclaration.toClassName()
        val expectedIntentTypeName = intentDeclaration.toClassName()

        // Validate 'state' parameter
        val stateParam = parameters["state"]
        if (stateParam == null) {
            logger.error("Function '$functionName' annotated with @MviScreen must have a 'state' parameter.", paneFunction)
        } else if (stateParam.type.toTypeName() != expectedStateTypeName) {
            logger.error(
                "Parameter 'state' in '$functionName' is of the wrong type. Expected: $expectedStateTypeName, Found: ${stateParam.type.toTypeName()}",
                stateParam,
            )
        }

        // Validate 'onIntent' parameter
        val onIntentParam = parameters["onIntent"]
        val expectedOnIntentType = LambdaTypeName.get(parameters = arrayOf(expectedIntentTypeName), returnType = UNIT)

        if (onIntentParam == null) {
            logger.error("Function '$functionName' annotated with @MviScreen must have an 'onIntent' parameter.", paneFunction)
        } else if (onIntentParam.type.toTypeName() != expectedOnIntentType) {
            logger.error(
                "Parameter 'onIntent' in '$functionName' is of the wrong type. Expected: $expectedOnIntentType, Found: ${onIntentParam.type.toTypeName()}",
                onIntentParam,
            )
        }

        // Validate 'uiEvents' parameter
        val uiEventsParam = parameters["uiEvents"]
        val expectedUiEventsType = MEMBER_FLOW.parameterizedBy(MEMBER_UI_EVENT)
        if (uiEventsParam == null) {
            // It's optional, so we just warn the user.
            logger.warn(
                "Function '$functionName' annotated with @MviScreen does not have a 'uiEvents' parameter. One-shot UI events will not be collected.",
                paneFunction,
            )
        } else if (uiEventsParam.type.toTypeName() != expectedUiEventsType) {
            logger.error(
                "Parameter 'uiEvents' in '$functionName' is of the wrong type. Expected: $expectedUiEventsType, Found: ${uiEventsParam.type.toTypeName()}",
                uiEventsParam,
            )
        }
    }

    private fun generateSideEffectMapClass(
        paneFunction: KSFunctionDeclaration,
        uiStateDeclaration: KSClassDeclaration,
        intentDeclaration: KSClassDeclaration,
        annotatedIntents: List<KSClassDeclaration>,
    ): ClassName {
        val packageName = paneFunction.packageName.asString()
        val baseName = paneFunction.simpleName.asString().removeSuffix("Pane")
        val sideEffectsRouterClassName = ClassName(packageName, "${baseName}SideEffectMap")

        val uiStateTypeName = uiStateDeclaration.toClassName()
        val intentTypeName = intentDeclaration.toClassName()
        val sideEffectInterface = MEMBER_SIDE_EFFECT.parameterizedBy(uiStateTypeName, intentTypeName)

        val fileBuilder = FileSpec.builder(sideEffectsRouterClassName.packageName, sideEffectsRouterClassName.simpleName)
        val classBuilder =
            TypeSpec
                .classBuilder(sideEffectsRouterClassName)
                .addSuperinterface(superinterface = MEMBER_SIDE_EFFECT_MAP.parameterizedBy(uiStateTypeName, intentTypeName))

        val constructorBuilder = FunSpec.constructorBuilder()
        val getFunctionBody =
            FunSpec
                .builder("get")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("intent", intentTypeName)
                .returns(sideEffectInterface.copy(nullable = true))
                .beginControlFlow("return when (intent)")

        annotatedIntents.forEach { intentClass ->
            val propName = getUniquePropName(intentClass, intentDeclaration)
            constructorBuilder.addParameter(
                ParameterSpec
                    .builder(propName, sideEffectInterface)
                    .build(),
            )
            classBuilder.addProperty(
                PropertySpec
                    .builder(propName, sideEffectInterface, KModifier.PRIVATE)
                    .initializer(propName)
                    .build(),
            )
            getFunctionBody.addStatement("is %T -> %L", intentClass.toClassName(), propName)
        }

        getFunctionBody.addStatement("else -> null").endControlFlow()
        classBuilder.primaryConstructor(constructorBuilder.build())
        classBuilder.addFunction(getFunctionBody.build())

        fileBuilder.addType(classBuilder.build()).build().writeTo(codeGenerator, Dependencies(true, paneFunction.containingFile!!))

        return sideEffectsRouterClassName
    }

    private fun generateSideEffectBuilderClass(
        paneFunction: KSFunctionDeclaration,
        uiStateDeclaration: KSClassDeclaration,
        intentDeclaration: KSClassDeclaration,
        annotatedIntents: List<KSClassDeclaration>,
    ): ClassName {
        val packageName = paneFunction.packageName.asString()
        val baseName = paneFunction.simpleName.asString().removeSuffix("Pane")
        val builderClassName = ClassName(packageName, "${baseName}SideEffectBuilder")

        val sideEffectInterface = MEMBER_SIDE_EFFECT.parameterizedBy(uiStateDeclaration.toClassName(), intentDeclaration.toClassName())

        val classBuilder = TypeSpec.classBuilder(builderClassName)
        annotatedIntents.forEach { intentClass ->
            val propName = getUniquePropName(intentClass, intentDeclaration)
            classBuilder.addProperty(
                PropertySpec
                    .builder(propName, sideEffectInterface)
                    .mutable(mutable = true)
                    .initializer("noOpSideEffect()")
                    .build(),
            )
        }

        FileSpec
            .builder(packageName, builderClassName.simpleName)
            .addImport("com.buildkt.mvi", "noOpSideEffect")
            .addType(classBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, paneFunction.containingFile!!))

        return builderClassName
    }

    private fun generateConfigClass(
        paneFunction: KSFunctionDeclaration,
        uiStateDeclaration: KSClassDeclaration,
        intentDeclaration: KSClassDeclaration,
        sideEffectBuilderClassName: ClassName,
        sideEffectMapClassName: ClassName,
        annotatedIntents: List<KSClassDeclaration>,
    ): ClassName {
        val packageName = paneFunction.packageName.asString()
        val baseName = paneFunction.simpleName.asString().removeSuffix("Pane")
        val configClassName = ClassName(packageName, "${baseName}Config")

        val uiStateTypeName = uiStateDeclaration.toClassName()
        val intentTypeName = intentDeclaration.toClassName()

        val reducerType = MEMBER_REDUCER.parameterizedBy(uiStateTypeName, intentTypeName)
        val middlewareType = MEMBER_MIDDLEWARE.parameterizedBy(uiStateTypeName, intentTypeName)
        val middlewareListType = ClassName("kotlin.collections", "MutableList").parameterizedBy(middlewareType)

        val sideEffectProperties = annotatedIntents.map { getUniquePropName(it, intentDeclaration) }
        val sideEffectsRouterArgs = sideEffectProperties.joinToString(", ") { "$it = builder.$it" }

        val configClass =
            TypeSpec
                .classBuilder(configClassName)
                .addProperty(
                    PropertySpec
                        .builder("reducer", reducerType)
                        .mutable(true)
                        .initializer("%T { state, _ -> state }", MEMBER_REDUCER) // Default no-op reducer
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("middlewares", middlewareListType)
                        .initializer("mutableListOf()")
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("sideEffects")
                        .addParameter(
                            "block",
                            LambdaTypeName.get(
                                receiver = sideEffectBuilderClassName,
                                returnType = UNIT,
                            ),
                        ).addStatement("val builder = %T()", sideEffectBuilderClassName)
                        .addStatement("builder.block()")
                        .addStatement("this.sideEffects = %T($sideEffectsRouterArgs)", sideEffectMapClassName)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("sideEffects", sideEffectMapClassName, KModifier.PRIVATE)
                        .mutable(mutable = true)
                        .addModifiers(KModifier.LATEINIT)
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("getSideEffects")
                        .returns(sideEffectMapClassName)
                        .addStatement("return sideEffects")
                        .build(),
                ).build()

        FileSpec
            .builder(packageName, configClassName.simpleName)
            .addType(configClass)
            .build()
            .writeTo(codeGenerator, Dependencies(true, paneFunction.containingFile!!))

        return configClassName
    }

    private fun generateViewModel(
        paneFunction: KSFunctionDeclaration,
        uiStateDeclaration: KSClassDeclaration,
        intentDeclaration: KSClassDeclaration,
        sideEffectMapClassName: ClassName,
        configClassName: ClassName,
    ): Pair<ClassName, ClassName> {
        val packageName = paneFunction.packageName.asString()
        val paneName = paneFunction.simpleName.asString()
        val viewModelName = paneName.replace("Pane", "ViewModel")
        val viewModelClassName = ClassName(packageName, viewModelName)

        val uiState = uiStateDeclaration.toClassName()
        val intent = intentDeclaration.toClassName()

        val viewModelSuperClass = MEMBER_MVI_VIEWMODEL.parameterizedBy(uiState, intent)
        val reducerInterface = MEMBER_REDUCER.parameterizedBy(uiState, intent)
        val middlewareListType = List::class.asClassName().parameterizedBy(MEMBER_MIDDLEWARE.parameterizedBy(uiState, intent))

        val viewModelClass =
            TypeSpec
                .classBuilder(viewModelClassName)
                .superclass(viewModelSuperClass)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("reducer", reducerInterface)
                        .addParameter("sideEffects", sideEffectMapClassName)
                        .addParameter("middlewares", middlewareListType)
                        .build(),
                ).addSuperclassConstructorParameter("initialState = %T()", uiState)
                .addSuperclassConstructorParameter("reducer = reducer")
                .addSuperclassConstructorParameter("sideEffects = sideEffects")
                .addSuperclassConstructorParameter("middlewares = middlewares")
                .build()

        val factoryClassName = ClassName(packageName, "${viewModelName}Factory")
        val factoryClass =
            TypeSpec
                .classBuilder(factoryClassName)
                .addSuperinterface(superinterface = MEMBER_VIEW_MODEL_PROVIDER_FACTORY)
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("config", configClassName).build())
                .addProperty(PropertySpec.builder("config", configClassName, KModifier.PRIVATE).initializer("config").build())
                .addFunction(
                    FunSpec
                        .builder("create")
                        .addModifiers(KModifier.OVERRIDE)
                        .addTypeVariable(TypeVariableName("T", MEMBER_ANDROID_VIEWMODEL))
                        .addParameter("modelClass", Class::class.asClassName().parameterizedBy(TypeVariableName("T")))
                        .returns(TypeVariableName("T"))
                        .addStatement(
                            "return %T(config.reducer, config.getSideEffects(), config.middlewares) as T",
                            viewModelClassName,
                        ).build(),
                ).build()

        FileSpec
            .builder(packageName, viewModelName)
            .addType(viewModelClass)
            .addType(factoryClass)
            .build()
            .writeTo(codeGenerator, Dependencies(true, paneFunction.containingFile!!))

        return Pair(viewModelClassName, factoryClassName)
    }

    private fun generateNavigationForPane(
        paneFunction: KSFunctionDeclaration,
        viewModelClassName: ClassName,
        factoryClassName: ClassName,
        configClassName: ClassName,
        navArguments: List<NavArgumentInfo>,
    ) {
        val packageName = paneFunction.packageName.asString()
        val paneName = paneFunction.simpleName.asString()
        val navFunName = paneName.replaceFirstChar { it.lowercase(Locale.getDefault()) }

        val fileBuilder =
            FileSpec
                .builder(packageName, "${paneName}Navigation")
                .addImport("androidx.compose.runtime", "getValue", "collectAsState")
                .addImport("androidx.lifecycle.viewmodel.compose", "viewModel")
                .addImport("androidx.navigation.compose", "composable")
                .addImport("com.buildkt.mvi.android", "CollectNavigationEvents")

        val navFunBuilder =
            FunSpec
                .builder(navFunName)
                .receiver(MEMBER_NAV_GRAPH_BUILDER)
                .addParameter("navController", MEMBER_NAV_CONTROLLER)
                .addParameter("route", String::class)
                .addParameter(
                    "config",
                    LambdaTypeName.get(
                        receiver = configClassName,
                        returnType = UNIT,
                    ),
                )

        val argumentExtractionBlock =
            navArguments.joinToString("\n") { navArg ->
                val name = navArg.name
                val typeName = navArg.typeName

                when (typeName) {
                    STRING -> """val $name = backStackEntry.arguments?.getString("$name")!!"""
                    LONG -> """val $name = backStackEntry.arguments?.getLong("$name") ?: 0L"""
                    INT -> """val $name = backStackEntry.arguments?.getInt("$name") ?: 0"""
                    BOOLEAN -> """val $name = backStackEntry.arguments?.getBoolean("$name") ?: false"""
                    FLOAT -> """val $name = backStackEntry.arguments?.getFloat("$name") ?: 0f"""
                    else -> {
                        logger.warn("Unsupported NavArgument type '$typeName' for '$name'.Mapping as Parcelable.")
                        """    val $name: $typeName = backStackEntry.arguments?.getParcelable("$name")!!"""
                    }
                }
            }

        val composableArgs = mutableListOf<String>()
        composableArgs.add("state = state")
        composableArgs.add("onIntent = viewModel::onIntent")

        val hasUiEventsParam = paneFunction.parameters.any { it.name?.asString() == "uiEvents" }
        if (hasUiEventsParam) {
            composableArgs.add("uiEvents = viewModel.uiEvents")
        }

        navArguments.forEach { navArg ->
            val argName = navArg.name
            composableArgs.add("$argName = $argName")
        }
        val composableArgsString = composableArgs.joinToString(separator = ",\n        ")

        navFunBuilder.addCode(
            """
            |composable(route = route) { backStackEntry ->
            |   val config = %T().apply(config)
            |   val viewModel: %T = viewModel(factory = %T(config))
            |   val state by viewModel.uiState.collectAsState()
            |
            |   CollectNavigationEvents(viewModel, navController)
            |
            |   $argumentExtractionBlock
            |   %M(
            |        $composableArgsString
            |   )
            |}
            """.trimMargin(),
            configClassName,
            viewModelClassName,
            factoryClassName,
            MemberName(paneFunction.packageName.asString(), paneFunction.simpleName.asString()),
        )

        fileBuilder
            .addFunction(navFunBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, paneFunction.containingFile!!))
    }

    private fun getUniquePropName(
        intentClass: KSClassDeclaration,
        rootIntent: KSClassDeclaration,
    ): String {
        val nameParts = mutableListOf<String>()
        var current: KSClassDeclaration? = intentClass
        while (current != null && current != rootIntent) {
            nameParts.add(current.simpleName.asString())
            current = current.parentDeclaration as? KSClassDeclaration
        }
        return nameParts
            .reversed()
            .joinToString(separator = "")
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }
    }

    companion object {
        val MEMBER_ANDROID_VIEWMODEL = ClassName("androidx.lifecycle", "ViewModel")
        val MEMBER_FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
        val MEMBER_MIDDLEWARE = ClassName("com.buildkt.mvi", "Middleware")
        val MEMBER_MVI_VIEWMODEL = ClassName("com.buildkt.mvi.android", "ViewModel")
        val MEMBER_NAV_CONTROLLER = ClassName("androidx.navigation", "NavController")
        val MEMBER_NAV_GRAPH_BUILDER = ClassName("androidx.navigation", "NavGraphBuilder")
        val MEMBER_REDUCER = ClassName("com.buildkt.mvi", "Reducer")
        val MEMBER_SIDE_EFFECT = ClassName("com.buildkt.mvi", "SideEffect")
        val MEMBER_SIDE_EFFECT_MAP = ClassName("com.buildkt.mvi", "SideEffectMap")
        val MEMBER_UI_EVENT = ClassName("com.buildkt.mvi.android", "UiEvent")
        val MEMBER_VIEW_MODEL_PROVIDER_FACTORY = ClassName("androidx.lifecycle", "ViewModelProvider", "Factory")
    }
}
