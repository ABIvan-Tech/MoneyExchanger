package com.s0l.moneyexchanger.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s0l.moneyexchanger.R
import com.s0l.moneyexchanger.model.CardData
import com.s0l.moneyexchanger.ui.MoneySign
import kotlinx.android.synthetic.main.card_item.view.*


class ExchangeInfoCardAdapter(
    private val sign: Boolean,//true - положительный баланс (приход), false - отрицательный баланс (расход)
    private val clickListener: (String) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var cardItemList: List<CardData> = emptyList()
    private var oppositeMoneyName: String = ""
    private var currentExchangeVolume: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    fun setDate(result: List<CardData>) {
        cardItemList = result as MutableList<CardData>
        oppositeMoneyName = cardItemList[0].moneyName
        notifyDataSetChanged()
    }

    fun getCard(position: Int): CardData {
        return cardItemList[position]
    }

    fun setOppositeMoneyName(oppositeMoneyName: String) {
        this.oppositeMoneyName = oppositeMoneyName
        updateCurrentExchangeVolume(0.0)
        notifyDataSetChanged()
    }

    fun updateCurrentExchangeVolume(volume: Double) {
        currentExchangeVolume = volume
        notifyDataSetChanged()
    }

    override fun getItemCount() = cardItemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CardViewHolder).bind(
            cardItemList[position],
            sign,
            currentExchangeVolume,
            oppositeMoneyName,
            clickListener
        )
    }

    class CardViewHolder(cardView: View) : RecyclerView.ViewHolder(cardView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            card: CardData,
            sign: Boolean,
            currentAmountForExchange: Double,
            oppositeMoneyName: String,
            clickListener: (String) -> Unit
        ) {
            itemView.tvName.text = card.moneyName
            if (currentAmountForExchange != 0.0)
                itemView.edAmount.setText("%.2f".format(if (sign) currentAmountForExchange else -1 * currentAmountForExchange))
            else
                itemView.edAmount.setText("")
            itemView.tvCurrentMoneyVolume.text = "You have: %.2f".format(card.currentMoneyVolume) +
                    MoneySign.getCurrencySymbol(card.moneyName)
            val exchangeRate: Double = card.currentExchangeRate[oppositeMoneyName] ?: error("")
            itemView.tvCurrentExchangeRate.text =
                MoneySign.getCurrencySymbol(card.moneyName) + "1 = " +
                        MoneySign.getCurrencySymbol(oppositeMoneyName) + "%.2f".format(exchangeRate)

            itemView.edAmount.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int, count: Int
                ) {
                    //обновляем информацию в колбеке той карточки у которой фокус ввода
                    if (itemView.edAmount.isFocused)
                        clickListener(s.toString())
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }
    }
}