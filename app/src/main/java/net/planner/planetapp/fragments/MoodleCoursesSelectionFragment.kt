package net.planner.planetapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.adapters.MoodleCoursesViewAdapter
import net.planner.planetapp.databinding.FragmentMoodleCoursesSelectionBinding
import net.planner.planetapp.viewmodels.MoodleCoursesSelectionFragmentViewModel


class MoodleCoursesSelectionFragment : Fragment() {
    companion object {

        private const val TAG = "MoodleCoursesSelectionFragment"

    }
    private lateinit var mBinding: FragmentMoodleCoursesSelectionBinding
    private lateinit var viewModel: MoodleCoursesSelectionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMoodleCoursesSelectionBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(MoodleCoursesSelectionFragmentViewModel::class.java)

        // Init Courses Recycler View
        val coursesRecycler = mBinding.coursesList
        coursesRecycler.layoutManager = LinearLayoutManager(context)
        coursesRecycler.adapter = MoodleCoursesViewAdapter(listOf())

        viewModel.courses.observe(viewLifecycleOwner, Observer { it?.let {
            val adapter = mBinding.coursesList.adapter as MoodleCoursesViewAdapter
            adapter.updateCourses(it.toList())
        } })

        mBinding.continueToSettingsButton.setOnClickListener { clicked->
            val adapter = mBinding.coursesList.adapter as MoodleCoursesViewAdapter
            val courses = adapter.courseIds


            val navController = findNavController()
            navController.navigate(MoodleCoursesSelectionFragmentDirections.actionMoodleCoursesSelectionFragmentToInitialSettingsFragment())
        }

        viewModel.updateCourses()

    }

}