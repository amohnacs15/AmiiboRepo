package com.amohnacs.amiiborepo.ui.main

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout.VERTICAL
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.amohnacs.amiiborepo.R
import com.amohnacs.amiiborepo.common.InjectionFragment
import com.amohnacs.amiiborepo.common.ViewModelFactory
import com.amohnacs.amiiborepo.databinding.FragmentMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_main_options.view.*
import javax.inject.Inject

class MainFragment : InjectionFragment(), AmiiboAdapter.AdapterCallback {

    @Inject lateinit var factory: ViewModelFactory<MainViewModel>

    private lateinit var viewModel: MainViewModel
    private var adapter: AmiiboAdapter = AmiiboAdapter(this)

    private var binding: FragmentMainBinding? = null
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var filterButton: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        mainDaggerComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        filterButton = binding?.bottomSheetLayout?.bottomSheet?.cardView?.filterButton
        binding?.bottomSheetLayout?.bottomSheet?.let {
            bottomSheetBehavior = BottomSheetBehavior.from(it)
        }
        filterButton?.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.filterAmiibos()
            it.isEnabled = false
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView.let {
            it?.layoutManager = StaggeredGridLayoutManager(getOrientationColumnCount(), VERTICAL)
            it?.adapter = adapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        viewModel.loadAmiibos()

        viewModel.amiibos.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            adapter.updateItems(it)
            filterButton?.isEnabled = true
        })
        viewModel.errorEvent.observe(viewLifecycleOwner, Observer { errorString ->
            binding?.root?.let { view ->
                Snackbar.make(view, errorString, Snackbar.LENGTH_LONG).show()
            }
        })
        viewModel.emptyStateEvent.observe(viewLifecycleOwner, Observer {
            binding?.emptyText?.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.loadingEvent.observe(viewLifecycleOwner, Observer {
            binding?.recyclerView?.visibility = if (it) View.GONE else View.VISIBLE
            binding?.loadingLayout?.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.purchasedEvent.observe(viewLifecycleOwner, Observer {
            if (it) {
                filterButton?.setBackgroundColor(requireActivity().getColor(R.color.primary_color))
                filterButton?.text = requireActivity().getString(R.string.view_all)
            } else {
                filterButton?.setBackgroundColor(requireActivity().getColor(android.R.color.holo_green_dark))
                filterButton?.text = requireActivity().getString(R.string.filter_purchased)
            }
        })
    }

    override fun onItemClicked(amiiboTail: String) {
        val actions = MainFragmentDirections.actionMainFragmentToDetailsFragment(amiiboTail)
        findNavController().navigate(actions)
    }

    private fun getOrientationColumnCount(newConfig: Configuration? = null): Int {
        return if (newConfig == null) {
            if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LANDSCAPE_COLUMN_COUNT
            } else {
                PORTRAIT_COLUMN_COUNT
            }
        } else {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LANDSCAPE_COLUMN_COUNT
            } else {
                PORTRAIT_COLUMN_COUNT
            }
        }
    }

    companion object {
        const val PORTRAIT_COLUMN_COUNT = 3
        const val LANDSCAPE_COLUMN_COUNT = 5
    }
}