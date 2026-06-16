package com.lagradost.cloudstream3.ui.music

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.MusicSongItemBinding
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage

class MusicSongAdapter(
    private val songs: MutableList<MusicSong> = mutableListOf(),
    private val onSongClick: ((MusicSong, Int) -> Unit)? = null
) : RecyclerView.Adapter<MusicSongAdapter.SongViewHolder>() {

    fun updateSongs(newSongs: List<MusicSong>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = MusicSongItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount() = songs.size

    inner class SongViewHolder(private val binding: MusicSongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: MusicSong, position: Int) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songDuration.text = formatDuration(song.duration)
            binding.songThumbnail.loadImage(song.thumbnail)
            binding.root.setOnClickListener {
                onSongClick?.invoke(song, position)
            }
        }
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }
}

class MusicSectionAdapter(
    private val sections: MutableList<MusicSection> = mutableListOf(),
    private val onSongClick: ((MusicSong, Int) -> Unit)? = null
) : RecyclerView.Adapter<MusicSectionAdapter.SectionViewHolder>() {

    fun updateSections(newSections: List<MusicSection>) {
        sections.clear()
        sections.addAll(newSections)
        notifyDataSetChanged()
    }

    override fun getItemCount() = sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = com.lagradost.cloudstream3.databinding.HomepageParentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    inner class SectionViewHolder(
        private val binding: com.lagradost.cloudstream3.databinding.HomepageParentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(section: MusicSection) {
            binding.homeChildMoreInfo.text = section.title

            val songAdapter = MusicSongAdapter(section.songs.toMutableList(), onSongClick)
            binding.homeChildRecyclerview.adapter = songAdapter
        }
    }
}
