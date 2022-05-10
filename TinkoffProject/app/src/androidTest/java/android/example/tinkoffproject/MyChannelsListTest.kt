package android.example.tinkoffproject

import android.example.tinkoffproject.screen.MyChannelsScreen
import androidx.test.core.app.ActivityScenario
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Test

class MyChannelsListTest : TestCase() {
    @Test
    fun showMyChannelsAfterClick_Expands_and_3rd_position_has_correct_name() = run {
        ActivityScenario.launch(MainActivity::class.java)
        step("Отображается список каналов") {
            MyChannelsScreen.channelsList.hasSize(2)
        }
        step("Отображается список топиков после клика") {
            MyChannelsScreen.channelsList.childAt<MyChannelsScreen.ChannelItem>(1) {
                click()
            }
            MyChannelsScreen.channelsList.hasSize(5)
            MyChannelsScreen.channelsList.childAt<MyChannelsScreen.ChannelItem>(2){
                name.hasText("(no topic)")
            }
        }
    }
}
