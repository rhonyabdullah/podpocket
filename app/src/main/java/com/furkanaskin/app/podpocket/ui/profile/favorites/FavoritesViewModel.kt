package com.furkanaskin.app.podpocket.ui.profile.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.furkanaskin.app.podpocket.core.BaseViewModel
import com.furkanaskin.app.podpocket.core.Resource
import com.furkanaskin.app.podpocket.core.Status
import com.furkanaskin.app.podpocket.db.AppDatabase
import com.furkanaskin.app.podpocket.service.PodpocketAPI
import com.furkanaskin.app.podpocket.service.response.Podcasts
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Created by Furkan on 2019-05-18
 */

class FavoritesViewModel @Inject constructor(api: PodpocketAPI, appDatabase: AppDatabase) :
    BaseViewModel(api, appDatabase) {

    private val _episodesLiveData = MutableLiveData<Resource<Podcasts>>()
    val episodesLiveData: LiveData<Resource<Podcasts>> get() = _episodesLiveData

    fun getEpisodes(id: String) {
        baseApi?.let { baseApi ->
            baseApi.getPodcastById(id)
                .subscribeOn(Schedulers.io())
                .map { Resource.success(it) }
                .onErrorReturn { Resource.error(it) }
                .doOnSubscribe { progressLiveData.postValue(true) }
                .doOnTerminate { progressLiveData.postValue(false) }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(this)
                .subscribe {
                    when (it?.status) {
                        Status.SUCCESS -> _episodesLiveData.postValue(it)
                        Status.LOADING -> ""
                        Status.ERROR -> Timber.e(it.error)
                    }
                }
        }
    }
}