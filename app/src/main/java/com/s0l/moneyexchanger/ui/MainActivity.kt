package com.s0l.moneyexchanger.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.s0l.moneyexchanger.Injection
import com.s0l.moneyexchanger.R
import com.s0l.moneyexchanger.adapter.ExchangeInfoCardAdapter
import com.s0l.moneyexchanger.model.CardData
import kotlinx.android.synthetic.main.main_activity.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


class MainActivity : MvpAppCompatActivity(R.layout.main_activity), MainView {

    companion object {
        private val TAG: String? = MainActivity::class.simpleName

        var amountCardOne: Double = 0.0
        var amountCardTwo: Double = 0.0
    }

    @InjectPresenter
    lateinit var mMainPresenter: MainPresenter

    @ProvidePresenter
    fun providePresenter(): MainPresenter = MainPresenter(Injection.provideMoneyDataSource(context = this))

    // Reference to the RecyclerView adapter
    private lateinit var cardOneAdapter: ExchangeInfoCardAdapter
    private lateinit var cardTwoAdapter: ExchangeInfoCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardOneAdapter =
            ExchangeInfoCardAdapter(false) { item: String -> amountInCardOneChanged(item) }
        cardTwoAdapter =
            ExchangeInfoCardAdapter(true) { item: String -> amountInCardTwoChanged(item) }

        viewPagerCurrOne.apply {
            visibility = View.GONE
            adapter = cardOneAdapter
        }
        viewPagerCurrTwo.apply {
            visibility = View.GONE
            adapter = cardTwoAdapter
        }
        showProgress()
    }

    override fun onResume() {
        super.onResume()

        mMainPresenter.getExchangeCurrency()

        viewPagerCurrOne.registerOnPageChangeCallback(callbackPagerCurrOne)
        viewPagerCurrTwo.registerOnPageChangeCallback(callbackPagerCurrTwo)
    }

    override fun onPause() {
        super.onPause()
        mMainPresenter.stopRefreshing()
        viewPagerCurrOne.unregisterOnPageChangeCallback(callbackPagerCurrOne)
        viewPagerCurrTwo.unregisterOnPageChangeCallback(callbackPagerCurrTwo)
    }

    private var callbackPagerCurrOne: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentCardOne: CardData = cardOneAdapter.getCard(position)
                val currentCardTwo: CardData = cardTwoAdapter.getCard(viewPagerCurrTwo.currentItem)
                cardOneAdapter.setOppositeMoneyName(currentCardTwo.moneyName)
                    .also { amountCardOne = 0.0 }
                cardTwoAdapter.setOppositeMoneyName(currentCardOne.moneyName)
                    .also { amountCardTwo = 0.0 }

                updateTitle()
            }
        }

    private var callbackPagerCurrTwo: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentCardTwo: CardData = cardTwoAdapter.getCard(position)
                val currentCardOne: CardData = cardOneAdapter.getCard(viewPagerCurrOne.currentItem)
                cardTwoAdapter.setOppositeMoneyName(currentCardOne.moneyName)
                    .also { amountCardTwo = 0.0 }
                cardOneAdapter.setOppositeMoneyName(currentCardTwo.moneyName)
                    .also { amountCardOne = 0.0 }

                updateTitle()
            }
        }

    fun updateTitle() {
        val currentCardOne: CardData = cardOneAdapter.getCard(viewPagerCurrOne.currentItem)
        val currentCardTwo: CardData = cardTwoAdapter.getCard(viewPagerCurrTwo.currentItem)
        val title =
            MoneySign.getCurrencySymbol(currentCardOne.moneyName) + "1 = " +
                    MoneySign.getCurrencySymbol(currentCardTwo.moneyName) + "%.2f"
                .format(currentCardOne.currentExchangeRate[currentCardTwo.moneyName] ?: error(""))

        setTitle(this.getString(R.string.app_name) + " $title")
    }

    //только вычисляем и отображаем на второй карточке сколько получим при конверсии
    //можно вынести вычисления в model
    private fun amountInCardOneChanged(amount: String?) {
        Log.d(TAG, "amountInCardOneChanged: $amount")
        val realAmount = amount?.replace("-", "")?.replace(",", ".")
        if (!realAmount.isNullOrEmpty()) {
            val currentCardOne: CardData = cardOneAdapter.getCard(viewPagerCurrOne.currentItem)
            val currentCardTwo: CardData = cardTwoAdapter.getCard(viewPagerCurrTwo.currentItem)
            amountCardOne = realAmount.toDouble()
            amountCardTwo =
                amountCardOne * currentCardOne.currentExchangeRate.getValue(currentCardTwo.moneyName)
            cardTwoAdapter.updateCurrentExchangeVolume(amountCardTwo)
        } else {
            amountCardOne = 0.0
            amountCardTwo = 0.0
            cardTwoAdapter.updateCurrentExchangeVolume(amountCardTwo)
        }
    }

    //только вычисляем и отображаем на первой карточке сколько получим при конверсии
    //можно вынести вычисления в model
    private fun amountInCardTwoChanged(amount: String?) {
        Log.d(TAG, "amountInCardTwoChanged: $amount")
        val realAmount = amount?.replace("-", "")?.replace(",", ".")
        if (!realAmount.isNullOrEmpty()) {
            val currentCardOne: CardData = cardOneAdapter.getCard(viewPagerCurrOne.currentItem)
            val currentCardTwo: CardData = cardTwoAdapter.getCard(viewPagerCurrTwo.currentItem)
            amountCardTwo = realAmount.toDouble()
            amountCardOne =
                amountCardTwo * currentCardTwo.currentExchangeRate.getValue(currentCardOne.moneyName)
            cardOneAdapter.updateCurrentExchangeVolume(amountCardOne)
        } else {
            amountCardOne = 0.0
            amountCardTwo = 0.0
            cardOneAdapter.updateCurrentExchangeVolume(amountCardOne)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.exchange_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.idExchange -> {
                doExchange()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //делаем обмен в итоге или обмен или ошибка
    private fun doExchange() {
        val currentCardOne: CardData = cardOneAdapter.getCard(viewPagerCurrOne.currentItem)
        val currentCardTwo: CardData = cardTwoAdapter.getCard(viewPagerCurrTwo.currentItem)
        mMainPresenter.doExchange(
            amountCardOne * -1, currentCardOne.moneyName,
            amountCardTwo, currentCardTwo.moneyName
        )
    }

    override fun updateUI() {
        hideProgress()

        cardOneAdapter.setDate(mMainPresenter.prepareInfoForCardOne())

        cardTwoAdapter.setDate(mMainPresenter.prepareInfoForCardTwo())

        if (amountCardOne != 0.0)
            cardOneAdapter.updateCurrentExchangeVolume(amountCardOne)
        if (amountCardTwo != 0.0)
            cardTwoAdapter.updateCurrentExchangeVolume(amountCardTwo)

        viewPagerCurrOne.visibility = View.VISIBLE
        viewPagerCurrTwo.visibility = View.VISIBLE
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun showError(message: String) {
        hideProgress()
        AlertDialog.Builder(this).setTitle(getString(R.string.error)).setMessage(message).create()
            .show()
    }

    override fun showError(messageCodes: ErrorCodes) {
        val message: String = when (messageCodes) {
            ErrorCodes.INSUFFICIENT_FUNDS -> this.getString(R.string.insufficient_funds)
            ErrorCodes.TRANSACTION_PROBLEM -> this.getString(R.string.transaction_problem)
        }
        showError(message)
    }

    override fun showSuccess(
        amountCardTwo: Double,
        moneyNameTwo: String,
        totalBalance: Map<String, Double>
    ) {
        var message: String = "Receipt " + MoneySign.getCurrencySymbol(moneyNameTwo) +
                "%.2f".format(amountCardTwo) + " to account $moneyNameTwo\n" +
                "Available balance " + MoneySign.getCurrencySymbol(moneyNameTwo) + "%.2f".format(totalBalance[moneyNameTwo]!!)
        message += "\n\n"
        message += "Available accounts:\n"
        totalBalance.map {
            message += "${it.key}: " + MoneySign.getCurrencySymbol(moneyNameTwo) + "%.2f".format(
                it.value
            ) + "\n"
        }

        Companion.amountCardOne = 0.0
        Companion.amountCardTwo = 0.0
        cardOneAdapter.updateCurrentExchangeVolume(Companion.amountCardOne)
        cardTwoAdapter.updateCurrentExchangeVolume(Companion.amountCardTwo)

        AlertDialog.Builder(this).setTitle(getString(R.string.transaction_info))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> }.create().show()
    }

}