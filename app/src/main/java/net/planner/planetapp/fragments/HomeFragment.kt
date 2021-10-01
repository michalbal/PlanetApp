package net.planner.planetapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.*
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.adapters.TasksViewAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.databinding.HomeFragmentBinding
import net.planner.planetapp.viewmodels.HomeFragmentViewModel

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var mBinding: HomeFragmentBinding

    private lateinit var nextEventAdapter: NextEventViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val userName = UserPreferencesManager.userName
        val insp = "Every day is a gift, that's why it's called the Present" // TODO have a list of strings

        viewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)

        mBinding = HomeFragmentBinding.inflate(inflater, container, false)

        // Add User information

        mBinding.helloText.text = String.format(App.context.getString(R.string.hello_greeting), userName)
        mBinding.inspText.text = insp

        // Init Next Events Recycler View
        val nextEventsRecycler = mBinding.nextEventsList
        nextEventsRecycler.layoutManager = LinearLayoutManager(context)
        nextEventsRecycler.adapter = NextEventViewAdapter(listOf())

        viewModel.eventsToDisplay.observe(viewLifecycleOwner, Observer { it?.let {
            val adapter = mBinding.nextEventsList.adapter as NextEventViewAdapter
            adapter.updateEvents(it.toList())
        } })


        // Init Tasks list Recycler View
        val tasksRecycler = mBinding.tasksList
        tasksRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val mainActivity = activity as? MainActivity
        tasksRecycler.adapter = TasksViewAdapter(listOf(), activity = mainActivity)

        val  today = System.currentTimeMillis()
        LocalDBManager.dbLocalTasksData.observe(viewLifecycleOwner, Observer { it?.let {
            // Sort tasks according to date and show from closest
            val sorted = it.toSortedSet(Comparator { o1, o2 ->
                if (o1.deadline < System.currentTimeMillis()) {
                    if(o2.deadline < System.currentTimeMillis()) {
                        o1.deadline.compareTo(o2.deadline)
                    } else {
                        -o1.deadline.compareTo(o2.deadline)
                    }
                } else {
                    o1.deadline.compareTo(o2.deadline)
                }
            })
            val adapter = mBinding.tasksList.adapter as TasksViewAdapter
            adapter.updateTasks(sorted.toList())
        } })

        return mBinding.root

    }

}