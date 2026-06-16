package com.lagradost.cloudstream3.ui.music

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.FragmentMusicSearchBinding
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage

class MusicSearchFragment : Fragment() {

    private var _binding: FragmentMusicSearchBinding? = null
    private val binding get() = _binding!!
    private val musicViewModel: MusicViewModel by activityViewModels()
    private lateinit var searchAdapter: MusicSongAdapter
    private var playerSheet: MusicPlayerSheet? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = MusicSongAdapter(onSongClick = { song, _ ->
            musicViewModel.playSong(song, listOf(song))
        })

        setupRecycler()
        setupSearchInput()
        setupClickListeners()
        setupObservers()
    }

    private fun setupRecycler() {
        binding.musicSearchRecycler.layoutManager = LinearLayoutManager(context)
        binding.musicSearchRecycler.adapter = searchAdapter
    }

    private fun setupSearchInput() {
        binding.musicSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.musicSearchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    musicViewModel.search(query)
                }
                true
            } else false
        }

        binding.musicSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 3) {
                    musicViewModel.search(query)
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.musicSearchBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.musicMiniPlayer.setOnClickListener {
            showPlayerSheet()
        }

        binding.miniPlayerPlayPause.setOnClickListener {
            musicViewModel.togglePlayPause()
        }

        binding.miniPlayerNext.setOnClickListener {
            musicViewModel.nextSong()
        }
    }

    private fun setupObservers() {
        musicViewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val songs = resource.value
                    searchAdapter.updateSongs(songs)
                    binding.musicSearchEmpty.isVisible = songs.isEmpty()
                    binding.musicSearchRecycler.isVisible = songs.isNotEmpty()
                }
                is Resource.Loading -> {
                    binding.musicSearchEmpty.isVisible = false
                }
                is Resource.Failure -> {
                    binding.musicSearchEmpty.isVisible = true
                    binding.musicSearchEmpty.text = "Search failed"
                }
            }
        }

        musicViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song == null) {
                binding.musicMiniPlayer.isVisible = false
            } else {
                binding.musicMiniPlayer.isVisible = true
                binding.miniPlayerTitle.text = song.title
                binding.miniPlayerArtist.text = song.artist
                binding.miniPlayerThumbnail.loadImage(song.thumbnail)
            }
        }

        musicViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            binding.miniPlayerPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24
            )
        }
    }

    private fun showPlayerSheet() {
        if (playerSheet == null) {
            playerSheet = MusicPlayerSheet()
        }
        playerSheet?.show(parentFragmentManager, "MusicPlayerSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
