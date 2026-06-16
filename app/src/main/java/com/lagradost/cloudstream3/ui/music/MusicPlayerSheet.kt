package com.lagradost.cloudstream3.ui.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.MusicPlayerSheetBinding
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage

class MusicPlayerSheet : BottomSheetDialogFragment() {

    private var _binding: MusicPlayerSheetBinding? = null
    private val binding get() = _binding!!
    private var viewModel: MusicViewModel? = null
    private var isUserSeeking = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = MusicPlayerSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as? androidx.fragment.app.FragmentActivity)
            ?.let { androidx.lifecycle.ViewModelProvider(it)[MusicViewModel::class.java] }

        setupObservers()
        setupControls()
    }

    private fun setupObservers() {
        viewModel?.currentSong?.observe(viewLifecycleOwner) { song ->
            if (song == null) return@observe
            binding.playerSongTitle.text = song.title
            binding.playerArtistName.text = song.artist
            binding.playerAlbumArt.loadImage(song.thumbnail)
        }

        viewModel?.isPlaying?.observe(viewLifecycleOwner) { playing ->
            binding.playerPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24
            )
        }

        viewModel?.currentPosition?.observe(viewLifecycleOwner) { pos ->
            if (!isUserSeeking) {
                binding.playerSeekbar.progress = pos
                binding.playerCurrentTime.text = formatTime(pos)
            }
        }

        viewModel?.duration?.observe(viewLifecycleOwner) { dur ->
            binding.playerSeekbar.max = if (dur > 0) dur else 100
            binding.playerTotalTime.text = formatTime(dur)
        }
    }

    private fun setupControls() {
        binding.playerPlayPause.setOnClickListener {
            viewModel?.togglePlayPause()
        }

        binding.playerNext.setOnClickListener {
            viewModel?.nextSong()
        }

        binding.playerPrevious.setOnClickListener {
            viewModel?.previousSong()
        }

        binding.playerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.playerCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = false
                viewModel?.seekTo(seekBar.progress)
            }
        })

        binding.playerShuffle.setOnClickListener {
            binding.playerShuffle.imageTintList = android.content.res.ColorStateList.valueOf(
                if (binding.playerShuffle.imageTintList?.defaultColor == 0xFF1ED760.toInt())
                    0xFF888888.toInt() else 0xFF1ED760.toInt()
            )
        }

        binding.playerRepeat.setOnClickListener {
            binding.playerRepeat.imageTintList = android.content.res.ColorStateList.valueOf(
                if (binding.playerRepeat.imageTintList?.defaultColor == 0xFF1ED760.toInt())
                    0xFF888888.toInt() else 0xFF1ED760.toInt()
            )
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
