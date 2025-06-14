package com.nabiilawidya.tehteksi.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabiilawidya.tehteksi.adapter.DiseaseAdapter
import com.nabiilawidya.tehteksi.databinding.FragmentHomeBinding
import com.nabiilawidya.tehteksi.ui.DiseaseActivity
import com.nabiilawidya.tehteksi.ui.history.HistoryActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPenyakit.layoutManager = LinearLayoutManager(requireContext())

        binding.btnLihatHistori.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        viewModel.penyakitList.observe(viewLifecycleOwner) { list ->
            val adapter = DiseaseAdapter(list) { disease ->
                val intent = Intent(requireContext(), DiseaseActivity::class.java).apply {
                    putExtra("nama", disease.nama)
                    putExtra("deskripsi", disease.deskripsi)
                    putExtra("gambar_url", disease.gambar_url)
                    putExtra("solusi", disease.solusi)
                }
                startActivity(intent)
            }
            binding.recyclerViewPenyakit.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
