package com.lagradost.cloudstream3.ui.music

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.mvvm.launchSafe
import com.lagradost.cloudstream3.mvvm.logError

class MusicViewModel : ViewModel() {
    private val _homeSections = MutableLiveData<Resource<List<MusicSection>>>()
    val homeSections: LiveData<Resource<List<MusicSection>>> = _homeSections

    private val _searchResults = MutableLiveData<Resource<List<MusicSong>>>()
    val searchResults: LiveData<Resource<List<MusicSong>>> = _searchResults

    private val _currentSong = MutableLiveData<MusicSong?>()
    val currentSong: LiveData<MusicSong?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _queue = MutableLiveData<List<MusicSong>>(emptyList())
    val queue: LiveData<List<MusicSong>> = _queue

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: MutableLiveData<Int> = _currentPosition

    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> = _duration

    private val _currentStreamUrl = MutableLiveData<String?>()
    val currentStreamUrl: LiveData<String?> = _currentStreamUrl

    private val _isLoadingStream = MutableLiveData(false)
    val isLoadingStream: LiveData<Boolean> = _isLoadingStream

    fun loadHomeSections() {
        viewModelScope.launchSafe {
            _homeSections.postValue(Resource.Loading())
            val result = MusicApi.getHomeSections()
            _homeSections.postValue(Resource.fromResult(result))
        }
    }

    fun search(query: String) {
        viewModelScope.launchSafe {
            _searchResults.postValue(Resource.Loading())
            val result = MusicApi.search(query)
            _searchResults.postValue(Resource.fromResult(result))
        }
    }

    fun playSong(song: MusicSong, songQueue: List<MusicSong>? = null) {
        viewModelScope.launchSafe {
            _currentSong.postValue(song)
            _queue.postValue(songQueue ?: listOf(song))
            _isPlaying.postValue(true)
            _isLoadingStream.postValue(true)

            val streamResult = MusicApi.getStreamUrl(song.videoId)
            streamResult.onSuccess { url ->
                _currentStreamUrl.postValue(url)
                _isLoadingStream.postValue(false)
            }.onFailure {
                logError(it)
                _isLoadingStream.postValue(false)
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.postValue(!(_isPlaying.value ?: false))
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.postValue(playing)
    }

    fun nextSong() {
        val queue = _queue.value ?: return
        val current = _currentSong.value ?: return
        val currentIndex = queue.indexOfFirst { it.videoId == current.videoId }
        if (currentIndex < queue.size - 1) {
            playSong(queue[currentIndex + 1], queue)
        }
    }

    fun previousSong() {
        val queue = _queue.value ?: return
        val current = _currentSong.value ?: return
        val currentIndex = queue.indexOfFirst { it.videoId == current.videoId }
        if (currentIndex > 0) {
            playSong(queue[currentIndex - 1], queue)
        }
    }

    fun seekTo(position: Int) {
        _currentPosition.postValue(position)
    }

    fun updatePosition(position: Int) {
        _currentPosition.postValue(position)
    }

    fun updateDuration(duration: Int) {
        _duration.postValue(duration)
    }

    fun clearSearch() {
        _searchResults.postValue(Resource.Loading())
    }
}
