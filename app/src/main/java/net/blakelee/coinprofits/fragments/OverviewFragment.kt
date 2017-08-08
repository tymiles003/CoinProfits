package net.blakelee.coinprofits.fragments

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.blakelee.coinprofits.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_overview.*
import net.blakelee.coinprofits.base.SwipeViewPager
import net.blakelee.coinprofits.models.Holdings
import net.blakelee.coinprofits.pricechart.Period
import net.blakelee.coinprofits.pricechart.PriceChart
import net.blakelee.coinprofits.pricechart.TimeFormatter
import net.blakelee.coinprofits.repository.ChartRepository
import net.blakelee.coinprofits.repository.HoldingsRepository
import net.blakelee.coinprofits.repository.PreferencesRepository
import net.blakelee.coinprofits.tools.textDim
import java.util.*
import javax.inject.Inject

class OverviewFragment : Fragment(), LifecycleRegistryOwner, AdapterView.OnItemSelectedListener {

    private lateinit var pager: SwipeViewPager
    private lateinit var spinner: Spinner
    @Inject lateinit var cRepo: ChartRepository
    @Inject lateinit var hRepo: HoldingsRepository
    @Inject lateinit var prefs: PreferencesRepository
    private val registry = LifecycleRegistry(this)
    private lateinit var chart: PriceChart
    private lateinit var adapter: ArrayAdapter<Holdings>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_overview, container, false)

    //Start Observables
    override fun onResume() {
        super.onResume()

        hRepo.getHoldings(prefs.ordered).subscribe {
            adapter.clear()
            adapter.addAll(it)
        }
    }

    private fun getChartData(id: String) {
        val last = Date().time
        val first = last - TimeFormatter(chart.period)

        cRepo.getChartData(id, first, last)
                .subscribe {
                    it.priceUsd?.let {
                        if (it.isNotEmpty()) {
                            chart_price.text = String.format("$%.2f", it.last()[1])
                            chart.addData(it)
                        }
                    }
                }
    }

    private fun updatePeriod(period: Period) {
        if (chart.period != period) {
            chart.period = period
            getChartData(((spinner.selectedItem) as Holdings).id)

            //This is super inefficient. I should make a lastSelected variable that it changes
            for(i in 0 until time_holder.childCount)
                time_holder.getChildAt(i).setBackgroundColor(ContextCompat.getColor(context, R.color.background))

            time_holder.getChildAt(period.value).setBackgroundColor(ContextCompat.getColor(context, R.color.cardColor))
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item: Holdings = adapter.getItem(position)

        spinner.layoutParams.width = textDim(item.toString(), context) + 500
        spinner.invalidate()
        getChartData(item.id)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    //Stop Observables
    override fun onPause() {
        super.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pager = activity.findViewById(R.id.pager)
        spinner = activity.findViewById(R.id.chart_items)
        adapter = ArrayAdapter(context, R.layout.spinner_row)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        chart = PriceChart(stacked_area_chart)
        chart.chart_date = chart_date
        chart.chart_price = chart_price
        chart.pager = pager
        day.setBackgroundColor(ContextCompat.getColor(context, R.color.cardColor))

        hour.setOnClickListener { updatePeriod(Period.HOUR) }
        day.setOnClickListener { updatePeriod(Period.DAY) }
        week.setOnClickListener { updatePeriod(Period.WEEK) }
        month.setOnClickListener { updatePeriod(Period.MONTH) }
        year.setOnClickListener { updatePeriod(Period.YEAR) }
        all.setOnClickListener { updatePeriod(Period.ALL) }
    }

    override fun getLifecycle(): LifecycleRegistry = registry

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}