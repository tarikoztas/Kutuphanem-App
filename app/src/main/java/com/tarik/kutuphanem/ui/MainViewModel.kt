package com.tarik.kutuphanem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarik.kutuphanem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface OturumDurumu {
    /** Firebase henuz cevap vermedi; acilista kisa sureli. */
    data object Bilinmiyor : OturumDurumu
    data object GirisYok : OturumDurumu
    data object GirisVar : OturumDurumu
}

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val oturum: StateFlow<OturumDurumu> = authRepository.oturumAkisi
        .map { if (it == null) OturumDurumu.GirisYok else OturumDurumu.GirisVar }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = OturumDurumu.Bilinmiyor
        )
}
