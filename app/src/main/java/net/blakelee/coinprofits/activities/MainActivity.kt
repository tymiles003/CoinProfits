package net.blakelee.coinprofits.activities

import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import kotlinx.android.synthetic.main.activity_main.*
import net.blakelee.coinprofits.R
import net.blakelee.coinprofits.adapters.MainPagerAdapter
import dagger.android.support.HasSupportFragmentInjector
import net.blakelee.coinprofits.base.SwipeViewPager
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    private val registry = LifecycleRegistry(this)
    override fun getLifecycle(): LifecycleRegistry = registry

    @Inject lateinit var fragmentAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val pager: SwipeViewPager = pager
        val pagerAdapter = MainPagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter
        pager.currentItem = 1
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentAndroidInjector
}
