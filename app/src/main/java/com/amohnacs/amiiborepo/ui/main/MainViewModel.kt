package com.amohnacs.amiiborepo.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amohnacs.amiiborepo.dagger.ActivityScope
import com.amohnacs.amiiborepo.domain.AmiiboRepo
import com.amohnacs.amiiborepo.model.Amiibo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.internal.notify
import javax.inject.Inject

class MainViewModel @Inject constructor(
        val amiiboRepo: AmiiboRepo
) : ViewModel() {

    val amiibos = MutableLiveData<List<Amiibo>>().apply { value = emptyList() }
    val errorEvent = MutableLiveData<String>()
    val emptyStateEvent = MutableLiveData<Any>()

    fun loadAmiibos() = amiiboRepo.fetchAmiibosInBulk()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { e -> errorEvent.value = e.message }
            .subscribe { amiiboResponse ->
                if (amiiboResponse.amiibos?.isNotEmpty() == true) {
                    amiibos.value = amiiboResponse.amiibos
                } else {
                    emptyStateEvent.value = Any()
                }
            }

}