package com.lagradost.cloudstream3.ui.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.FragmentMusicHomeBinding
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage
import com.lagradost.cloudstream3.utils.UIHelper.navigate

class MusicHomeFragment : Fragment() {

    private var _binding: FragmentMusicHomeBinding? = null
    private val binding get() = _binding!!
    private val musicViewModel: MusicViewModel by activityViewModels()
    private var playerSheet: MusicPlayerSheet? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
        musicViewModel.loadHomeSections()
    }

    private fun setupClickListeners() {
        binding.musicHomeSearchBtn.setOnClickListener {
            requireActivity().navigate(R.id.navigation_music_search)
        }

        binding.miniPlayer.musicMiniPlayer.setOnClickListener {
            showPlayerSheet()
        }

        binding.miniPlayer.miniPlayerPlayPause.setOnClickListener {
            musicViewModel.togglePlayPause()
        }

        binding.miniPlayer.miniPlayerNext.setOnClickListener {
            musicViewModel.nextSong()
        }

        binding.musicHomeTitle.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        musicViewModel.homeSections.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val sections = resource.value
                    if (sections.isEmpty()) return@observe
                    val adapter = MusicSectionAdapter(sections.toMutableList()) { song, _ ->
                        musicViewModel.playSong(song, sections.firstOrNull()?.songs ?: listOf(song))
                    }
                    binding.musicHomeRecycler.layoutManager = LinearLayoutManager(context)
                    binding.musicHomeRecycler.adapter = adapter
                }
                is Resource.Loading -> {}
                is Resource.Failure -> {}
            }
        }

        musicViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song == null) {
                binding.miniPlayer.musicMiniPlayer.isVisible = false
            } else {
                binding.miniPlayer.musicMiniPlayer.isVisible = true
                binding.miniPlayer.miniPlayerTitle.text = song.title
                binding.miniPlayer.miniPlayerArtist.text = song.artist
                binding.miniPlayer.miniPlayerThumbnail.loadImage(song.thumbnail)
            }
        }

        musicViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            binding.miniPlayer.miniPlayerPlayPause.setImageResource(
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
