package com.buildkt.feature.address

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.buildkt.feature.address.domain.AddressRepository
import com.buildkt.feature.address.presentation.create.createAddress
import com.buildkt.feature.address.presentation.create.createAddressPane
import com.buildkt.feature.address.presentation.create.createAddressReducer
import com.buildkt.feature.address.presentation.create.createAddressValidateCity
import com.buildkt.feature.address.presentation.create.createAddressValidateCountry
import com.buildkt.feature.address.presentation.create.createAddressValidateStreet
import com.buildkt.feature.address.presentation.create.createAddressValidateZip
import com.buildkt.feature.address.presentation.edit.editAddress
import com.buildkt.feature.address.presentation.edit.editAddressPane
import com.buildkt.feature.address.presentation.edit.editAddressReducer
import com.buildkt.feature.address.presentation.edit.editAddressValidateCity
import com.buildkt.feature.address.presentation.edit.editAddressValidateCountry
import com.buildkt.feature.address.presentation.edit.editAddressValidateStreet
import com.buildkt.feature.address.presentation.edit.editAddressValidateZip
import com.buildkt.feature.address.presentation.edit.loadAddress
import com.buildkt.feature.address.presentation.listing.AddressListIntent
import com.buildkt.feature.address.presentation.listing.addressListPane
import com.buildkt.feature.address.presentation.listing.addressListReducer
import com.buildkt.feature.address.presentation.listing.loadAddresses
import com.buildkt.mvi.android.NavigationEvent
import com.buildkt.mvi.android.logMiddleware
import com.buildkt.mvi.android.navigate
import com.buildkt.mvi.android.routeTo
import com.buildkt.mvi.android.showToast

fun NavGraphBuilder.addressFlowNavigation(
    navController: NavController,
    route: String,
    addressRepository: AddressRepository,
) = navigation(
    route = route,
    startDestination = LISTING_PANE_ROUTE,
) {
    addressListPane(navController, route = LISTING_PANE_ROUTE) {
        timeTravelDebugging {
            enable = true
        }

        reducer = addressListReducer()

        sideEffects {
            backClicked = navigate(event = NavigationEvent.PopBack)
            paneLaunched = loadAddresses(repository = addressRepository)

            addressSelected =
                navigate { _, intent ->
                    intent as AddressListIntent.AddressSelected
                    NavigationEvent.PopBackWithResult(key = ADDRESS_FLOW_RESULT, result = intent.addressId)
                }

            addNewAddress = routeTo { _, _ -> CREATE_PANE_ROUTE }

            editAddress =
                routeTo { _, intent ->
                    intent as AddressListIntent.EditAddress
                    editAddressRoute(addressId = intent.addressId)
                }
        }
    }

    createAddressPane(navController, route = CREATE_PANE_ROUTE) {
        middlewares += logMiddleware()

        reducer = createAddressReducer()

        sideEffects {
            backClicked = navigate(event = NavigationEvent.PopBack)

            streetChanged = createAddressValidateStreet
            cityChanged = createAddressValidateCity
            zipChanged = createAddressValidateZip
            countryChanged = createAddressValidateCountry

            saveAddress = createAddress(repository = addressRepository)
            saveAddressResultSuccess = navigate(event = NavigationEvent.PopBack)
            saveAddressResultFailure = showToast(message = "Ops! Something went wrong")
        }
    }

    editAddressPane(navController, route = EDIT_PANE_ROUTE) {
        middlewares += logMiddleware()

        reducer = editAddressReducer()

        sideEffects {
            backClicked = navigate(event = NavigationEvent.PopBack)

            loadAddress = loadAddress(repository = addressRepository)

            streetChanged = editAddressValidateStreet
            cityChanged = editAddressValidateCity
            zipChanged = editAddressValidateZip
            countryChanged = editAddressValidateCountry

            editAddress = editAddress(repository = addressRepository)
            editAddressResultSuccess = navigate(event = NavigationEvent.PopBack)
            editAddressResultFailure = showToast(message = "Ops! Something went wrong")
        }
    }
}

const val ADDRESS_FLOW_ROUTE = "addresses"
const val ADDRESS_FLOW_RESULT = "addresses/result"

private const val LISTING_PANE_ROUTE = "addresses/listing"
private const val CREATE_PANE_ROUTE = "addresses/create"
private const val EDIT_PANE_ROUTE = "addresses/edit?addressId={addressId}"

private fun editAddressRoute(addressId: Long) = EDIT_PANE_ROUTE.replace("{addressId}", addressId.toString())
