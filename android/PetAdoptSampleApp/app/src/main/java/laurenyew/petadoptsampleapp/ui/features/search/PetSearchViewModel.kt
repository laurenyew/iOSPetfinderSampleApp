package laurenyew.petadoptsampleapp.ui.features.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import laurenyew.petadoptsampleapp.repository.PetFavoriteRepository
import laurenyew.petadoptsampleapp.repository.PetSearchRepository
import laurenyew.petadoptsampleapp.repository.responses.SearchPetsRepoResponse
import laurenyew.petadoptsampleapp.ui.features.list.PetListViewModel
import javax.inject.Inject

@HiltViewModel
class PetSearchViewModel @Inject constructor(
    private val searchRepository: PetSearchRepository,
    private val favoriteRepository: PetFavoriteRepository
) : PetListViewModel(favoriteRepository) {
    val location: MutableState<String> = mutableStateOf("")

    fun searchAnimals() {
        val newLocation = location.value
        if (newLocation.isNotBlank() && newLocation.length >= 5) {
            _isLoading.value = true

            viewModelScope.launch {
                when (val searchResponse = searchRepository.getNearbyDogs(newLocation)) {
                    is SearchPetsRepoResponse.Success -> {
                        val favorites =
                            favoriteRepository.favoriteIds()
                        val searchedAnimals =
                            searchResponse.animals?.map {
                                if (favorites.contains(it.id)) {
                                    it.copy(true)
                                } else {
                                    it
                                }
                            }

                        _animals.value = searchedAnimals ?: emptyList()
                        _isError.value = false
                    }
                    else -> {
                        _animals.value = emptyList()
                        _isError.value = true
                    }
                }
                _isLoading.value = false
            }
        }
    }
}