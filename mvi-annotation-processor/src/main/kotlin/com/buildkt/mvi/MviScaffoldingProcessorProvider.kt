package com.buildkt.mvi

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class MviScaffoldingProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = MviScaffoldingProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger,
    )
}
