package com.marzinp.badminton.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.marzinp.badminton.R
import com.marzinp.badminton.ViewModel.TeamViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamFragment : Fragment() {

    companion object {
        fun newInstance() = TeamFragment()
    }

    private val viewModel: TeamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_team, container, false)
    }
}