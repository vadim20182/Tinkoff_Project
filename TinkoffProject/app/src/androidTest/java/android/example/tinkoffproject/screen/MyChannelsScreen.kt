package android.example.tinkoffproject.screen

import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.ui.my.MyChannelsFragment
import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object MyChannelsScreen : KScreen<MyChannelsScreen>() {
    override val layoutId: Int = R.layout.my_channels_fragment
    override val viewClass: Class<*> = MyChannelsFragment::class.java

    val channelsList =
        KRecyclerView(
            {
                withId(R.id.recycler_view_my_channels)
            },
            { itemType { ChannelItem(it) } })

    class ChannelItem(parent: Matcher<View>) : KRecyclerItem<ChannelItem>(parent) {
        val name = KTextView(parent){withId(R.id.topic_item_name)}
    }
}