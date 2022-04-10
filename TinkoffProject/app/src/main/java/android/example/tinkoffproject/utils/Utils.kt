package android.example.tinkoffproject.utils

import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.channels.ui.ChannelsAdapter
import android.example.tinkoffproject.contacts.ui.ContactsAdapter
import android.example.tinkoffproject.contacts.ui.ContactsViewModel
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit

fun <T : RecyclerView.Adapter<RecyclerView.ViewHolder>> makeSearchDisposable(
    querySearch: PublishSubject<Boolean>,
    shimmer: ShimmerFrameLayout,
    recyclerView: RecyclerView,
    recyclerAdapter: T,
    viewModel: ViewModel
) = querySearch
    .subscribeOn(Schedulers.io())
    .switchMapSingle {
        val queryTimeoutReset = SingleSubject.create<Boolean>()
        val disposableTimeout = queryTimeoutReset
            .subscribeOn(Schedulers.io())
            .timeout(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                shimmer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            })
        Single.fromCallable {
            Pair(
                it, when (recyclerAdapter) {
                    is ContactsAdapter -> {
                        (recyclerAdapter as ContactsAdapter).update((viewModel as ContactsViewModel).currentContacts)
                    }

                    else -> {
                        (recyclerAdapter as ChannelsAdapter).update((viewModel as BaseChannelsViewModel).currentChannels)
                    }
                }
            )
        }
            .subscribeOn(Schedulers.io())
            .doAfterSuccess {
                queryTimeoutReset.onSuccess(true)
                disposableTimeout.dispose()
            }
    }
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeBy(onNext = {
        when (recyclerAdapter) {
            is ContactsAdapter -> {
                (recyclerAdapter as ContactsAdapter).data =
                    (viewModel as ContactsViewModel).currentContacts
            }
            else -> {
                (recyclerAdapter as ChannelsAdapter).data =
                    (viewModel as BaseChannelsViewModel).currentChannels
            }
        }
        it.second.dispatchUpdatesTo(recyclerAdapter)
        recyclerView.scrollToPosition(0)
        if (!it.first) {
            shimmer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            shimmer.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    })

fun makeSearchObservable(
    querySearch: Observable<String>,
    resetSearch: () -> Unit
): Observable<String> = querySearch
    .map { query -> query.trim() }
    .scan { previous, current ->
        if (current.isBlank() && previous.isNotBlank())
            resetSearch()
        current
    }
    .filter { it.isNotBlank() }
    .distinctUntilChanged()

fun <T> makePublishSubject() = PublishSubject.create<T>()

val EMOJI_MAP = mutableMapOf<String, Int>().apply {
    put("grinning", 0x1F600)
    put("smiley", 0x1F603)
    put("big_smile", 0x1F604)
    put("grinning_face_with_smiling_eyes", 0x1F601)
    put("laughing", 0x1F606)
    put("sweat_smile", 0x1F605)
    put("joy", 0x1F602)
    put("rolling_on_the_floor_laughing", 0x1F923)
    put("smiling_face", 0x263A)
    put("blush", 0x1F60A)
    put("innocent", 0x1F607)
    put("smile", 0x1F642)
    put("upside_down", 0x1F643)
    put("wink", 0x1F609)
    put("relieved", 0x1F60C)
    put("heart_eyes", 0x1F60D)
    put("heart_kiss", 0x1F618)
    put("kiss", 0x1F617)
    put("kiss_smiling_eyes", 0x1F619)
    put("kiss_with_blush", 0x1F61A)
    put("yum", 0x1F60B)
    put("stuck_out_tongue", 0x1F61B)
    put("stuck_out_tongue_wink", 0x1F61C)
    put("stuck_out_tongue_closed_eyes", 0x1F61D)
    put("money_face", 0x1F911)
    put("hug", 0x1F917)
    put("nerd", 0x1F913)
    put("sunglasses", 0x1F60E)
    put("clown", 0x1F921)
    put("cowboy", 0x1F920)
    put("smirk", 0x1F60F)
    put("unamused", 0x1F612)
    put("disappointed", 0x1F61E)
    put("pensive", 0x1F614)
    put("worried", 0x1F61F)
    put("oh_no", 0x1F615)
    put("frown", 0x1F641)
    put("sad", 0x2639)
    put("persevere", 0x1F623)
    put("confounded", 0x1F616)
    put("anguish", 0x1F62B)
    put("weary", 0x1F629)
    put("triumph", 0x1F624)
    put("angry", 0x1F620)
    put("rage", 0x1F621)
    put("speechless", 0x1F636)
    put("neutral", 0x1F610)
    put("expressionless", 0x1F611)
    put("hushed", 0x1F62F)
    put("frowning", 0x1F626)
    put("anguished", 0x1F627)
    put("open_mouth", 0x1F62E)
    put("astonished", 0x1F632)
    put("dizzy", 0x1F635)
    put("flushed", 0x1F633)
    put("scream", 0x1F631)
    put("fear", 0x1F628)
    put("cold_sweat", 0x1F630)
    put("cry", 0x1F622)
    put("exhausted", 0x1F625)
    put("drooling", 0x1F924)
    put("sob", 0x1F62D)
    put("sweat", 0x1F613)
    put("sleepy", 0x1F62A)
    put("sleeping", 0x1F634)
    put("rolling_eyes", 0x1F644)
    put("thinking", 0x1F914)
    put("lying", 0x1F925)
    put("grimacing", 0x1F62C)
    put("silence", 0x1F910)
    put("nauseated", 0x1F922)
    put("sneezing", 0x1F927)
    put("mask", 0x1F637)
    put("sick", 0x1F912)
    put("hurt", 0x1F915)
    put("smiling_devil", 0x1F608)
    put("devil", 0x1F47F)
    put("ogre", 0x1F479)
    put("goblin", 0x1F47A)
    put("poop", 0x1F4A9)
    put("ghost", 0x1F47B)
    put("skull", 0x1F480)
    put("skull_and_crossbones", 0x2620)
    put("alien", 0x1F47D)
    put("space_invader", 0x1F47E)
    put("robot", 0x1F916)
    put("jack-o-lantern", 0x1F383)
    put("smiley_cat", 0x1F63A)
    put("smile_cat", 0x1F638)
    put("joy_cat", 0x1F639)
    put("heart_eyes_cat", 0x1F63B)
    put("smirk_cat", 0x1F63C)
    put("kissing_cat", 0x1F63D)
    put("scream_cat", 0x1F640)
    put("crying_cat", 0x1F63F)
    put("angry_cat", 0x1F63E)
    put("open_hands", 0x1F450)
    put("raised_hands", 0x1F64C)
    put("clap", 0x1F44F)
    put("pray", 0x1F64F)
    put("handshake", 0x1F91D)
    put("+1", 0x1F44D)
    put("-1", 0x1F44E)
    put("fist_bump", 0x1F44A)
    put("fist", 0x270A)
    put("left_fist", 0x1F91B)
    put("right_fist", 0x1F91C)
    put("fingers_crossed", 0x1F91E)
    put("peace_sign", 0x270C)
    put("rock_on", 0x1F918)
    put("ok", 0x1F44C)
    put("point_left", 0x1F448)
    put("point_right", 0x1F449)
    put("point_up", 0x1F446)
    put("point_down", 0x1F447)
    put("wait_one_second", 0x261D)
    put("hand", 0x270B)
    put("stop", 0x1F91A)
    put("high_five", 0x1F590)
    put("spock", 0x1F596)
    put("wave", 0x1F44B)
    put("call_me", 0x1F919)
    put("muscle", 0x1F4AA)
    put("middle_finger", 0x1F595)
    put("writing", 0x270D)
    put("selfie", 0x1F933)
    put("nail_polish", 0x1F485)
    put("ring", 0x1F48D)
    put("lipstick", 0x1F484)
    put("lipstick_kiss", 0x1F48B)
    put("lips", 0x1F444)
    put("tongue", 0x1F445)
    put("ear", 0x1F442)
    put("nose", 0x1F443)
    put("footprints", 0x1F463)
    put("eye", 0x1F441)
    put("eyes", 0x1F440)
    put("speaking_head", 0x1F5E3)
    put("silhouette", 0x1F464)
    put("silhouettes", 0x1F465)
    put("baby", 0x1F476)
    put("boy", 0x1F466)
    put("girl", 0x1F467)
    put("man", 0x1F468)
    put("woman", 0x1F469)
    put("older_man", 0x1F474)
    put("older_woman", 0x1F475)
    put("gua_pi_mao", 0x1F472)
    put("turban", 0x1F473)
    put("police", 0x1F46E)
    put("construction_worker", 0x1F477)
    put("guard", 0x1F482)
    put("detective", 0x1F575)
    put("mother_christmas", 0x1F936)
    put("santa", 0x1F385)
    put("princess", 0x1F478)
    put("prince", 0x1F934)
    put("bride", 0x1F470)
    put("tuxedo", 0x1F935)
    put("angel", 0x1F47C)
    put("pregnant", 0x1F930)
    put("bow", 0x1F647)
    put("information_desk_person", 0x1F481)
    put("no_signal", 0x1F645)
    put("ok_signal", 0x1F646)
    put("raising_hand", 0x1F64B)
    put("face_palm", 0x1F926)
    put("shrug", 0x1F937)
    put("person_pouting", 0x1F64E)
    put("person_frowning", 0x1F64D)
    put("haircut", 0x1F487)
    put("massage", 0x1F486)
    put("levitating", 0x1F574)
    put("dancer", 0x1F483)
    put("dancing", 0x1F57A)
    put("dancers", 0x1F46F)
    put("walking", 0x1F6B6)
    put("running", 0x1F3C3)
    put("man_and_woman_holding_hands", 0x1F46B)
    put("two_women_holding_hands", 0x1F46D)
    put("two_men_holding_hands", 0x1F46C)
    put("family", 0x1F46A)
    put("clothing", 0x1F45A)
    put("shirt", 0x1F455)
    put("jeans", 0x1F456)
    put("tie", 0x1F454)
    put("dress", 0x1F457)
    put("bikini", 0x1F459)
    put("kimono", 0x1F458)
    put("high_heels", 0x1F460)
    put("sandal", 0x1F461)
    put("boot", 0x1F462)
    put("shoe", 0x1F45E)
    put("athletic_shoe", 0x1F45F)
    put("hat", 0x1F452)
    put("top_hat", 0x1F3A9)
    put("graduate", 0x1F393)
    put("crown", 0x1F451)
    put("helmet", 0x26D1)
    put("backpack", 0x1F392)
    put("pouch", 0x1F45D)
    put("purse", 0x1F45B)
    put("handbag", 0x1F45C)
    put("briefcase", 0x1F4BC)
    put("glasses", 0x1F453)
    put("dark_sunglasses", 0x1F576)
    put("closed_umbrella", 0x1F302)
    put("umbrella", 0x2602)
    put("puppy", 0x1F436)
    put("kitten", 0x1F431)
    put("dormouse", 0x1F42D)
    put("hamster", 0x1F439)
    put("bunny", 0x1F430)
    put("fox", 0x1F98A)
    put("bear", 0x1F43B)
    put("panda", 0x1F43C)
    put("koala", 0x1F428)
    put("tiger_cub", 0x1F42F)
    put("lion", 0x1F981)
    put("calf", 0x1F42E)
    put("piglet", 0x1F437)
    put("pig_nose", 0x1F43D)
    put("frog", 0x1F438)
    put("monkey_face", 0x1F435)
    put("see_no_evil", 0x1F648)
    put("hear_no_evil", 0x1F649)
    put("speak_no_evil", 0x1F64A)
    put("monkey", 0x1F412)
    put("chicken", 0x1F414)
    put("penguin", 0x1F427)
    put("bird", 0x1F426)
    put("chick", 0x1F424)
    put("hatching", 0x1F423)
    put("new_baby", 0x1F425)
    put("duck", 0x1F986)
    put("eagle", 0x1F985)
    put("owl", 0x1F989)
    put("bat", 0x1F987)
    put("wolf", 0x1F43A)
    put("boar", 0x1F417)
    put("pony", 0x1F434)
    put("unicorn", 0x1F984)
    put("bee", 0x1F41D)
    put("bug", 0x1F41B)
    put("butterfly", 0x1F98B)
    put("snail", 0x1F40C)
    put("shell", 0x1F41A)
    put("beetle", 0x1F41E)
    put("ant", 0x1F41C)
    put("spider", 0x1F577)
    put("web", 0x1F578)
    put("turtle", 0x1F422)
    put("snake", 0x1F40D)
    put("lizard", 0x1F98E)
    put("scorpion", 0x1F982)
    put("crab", 0x1F980)
    put("squid", 0x1F991)
    put("octopus", 0x1F419)
    put("shrimp", 0x1F990)
    put("tropical_fish", 0x1F420)
    put("fish", 0x1F41F)
    put("blowfish", 0x1F421)
    put("dolphin", 0x1F42C)
    put("shark", 0x1F988)
    put("whale", 0x1F433)
    put("humpback_whale", 0x1F40B)
    put("crocodile", 0x1F40A)
    put("leopard", 0x1F406)
    put("tiger", 0x1F405)
    put("water_buffalo", 0x1F403)
    put("ox", 0x1F402)
    put("cow", 0x1F404)
    put("deer", 0x1F98C)
    put("arabian_camel", 0x1F42A)
    put("camel", 0x1F42B)
    put("elephant", 0x1F418)
    put("rhinoceros", 0x1F98F)
    put("gorilla", 0x1F98D)
    put("horse", 0x1F40E)
    put("pig", 0x1F416)
    put("goat", 0x1F410)
    put("ram", 0x1F40F)
    put("sheep", 0x1F411)
    put("dog", 0x1F415)
    put("poodle", 0x1F429)
    put("cat", 0x1F408)
    put("rooster", 0x1F413)
    put("turkey", 0x1F983)
    put("dove", 0x1F54A)
    put("rabbit", 0x1F407)
    put("mouse", 0x1F401)
    put("rat", 0x1F400)
    put("chipmunk", 0x1F43F)
    put("paw_prints", 0x1F43E)
    put("dragon", 0x1F409)
    put("dragon_face", 0x1F432)
    put("cactus", 0x1F335)
    put("holiday_tree", 0x1F384)
    put("evergreen_tree", 0x1F332)
    put("tree", 0x1F333)
    put("palm_tree", 0x1F334)
    put("seedling", 0x1F331)
    put("herb", 0x1F33F)
    put("shamrock", 0x2618)
    put("lucky", 0x1F340)
    put("bamboo", 0x1F38D)
    put("wish_tree", 0x1F38B)
    put("leaves", 0x1F343)
    put("fallen_leaf", 0x1F342)
    put("maple_leaf", 0x1F341)
    put("mushroom", 0x1F344)
    put("harvest", 0x1F33E)
    put("bouquet", 0x1F490)
    put("tulip", 0x1F337)
    put("rose", 0x1F339)
    put("wilted_flower", 0x1F940)
    put("sunflower", 0x1F33B)
    put("blossom", 0x1F33C)
    put("cherry_blossom", 0x1F338)
    put("hibiscus", 0x1F33A)
    put("earth_americas", 0x1F30E)
    put("earth_africa", 0x1F30D)
    put("earth_asia", 0x1F30F)
    put("full_moon", 0x1F315)
    put("new_moon", 0x1F311)
    put("waxing_moon", 0x1F314)
    put("new_moon_face", 0x1F31A)
    put("moon_face", 0x1F31D)
    put("sun_face", 0x1F31E)
    put("goodnight", 0x1F31B)
    put("moon", 0x1F319)
    put("seeing_stars", 0x1F4AB)
    put("star", 0x2B50)
    put("glowing_star", 0x1F31F)
    put("sparkles", 0x2728)
    put("high_voltage", 0x26A1)
    put("fire", 0x1F525)
    put("boom", 0x1F4A5)
    put("comet", 0x2604)
    put("sunny", 0x2600)
    put("mostly_sunny", 0x1F324)
    put("partly_sunny", 0x26C5)
    put("cloudy", 0x1F325)
    put("sunshowers", 0x1F326)
    put("rainbow", 0x1F308)
    put("cloud", 0x2601)
    put("rainy", 0x1F327)
    put("thunderstorm", 0x26C8)
    put("lightning", 0x1F329)
    put("snowy", 0x1F328)
    put("snowman", 0x2603)
    put("frosty", 0x26C4)
    put("snowflake", 0x2744)
    put("windy", 0x1F32C)
    put("dash", 0x1F4A8)
    put("tornado", 0x1F32A)
    put("fog", 0x1F32B)
    put("ocean", 0x1F30A)
    put("drop", 0x1F4A7)
    put("sweat_drops", 0x1F4A6)
    put("umbrella_with_rain", 0x2614)
    put("green_apple", 0x1F34F)
    put("apple", 0x1F34E)
    put("pear", 0x1F350)
    put("orange", 0x1F34A)
    put("lemon", 0x1F34B)
    put("banana", 0x1F34C)
    put("watermelon", 0x1F349)
    put("grapes", 0x1F347)
    put("strawberry", 0x1F353)
    put("melon", 0x1F348)
    put("cherries", 0x1F352)
    put("peach", 0x1F351)
    put("pineapple", 0x1F34D)
    put("kiwi", 0x1F95D)
    put("avocado", 0x1F951)
    put("tomato", 0x1F345)
    put("eggplant", 0x1F346)
    put("cucumber", 0x1F952)
    put("carrot", 0x1F955)
    put("corn", 0x1F33D)
    put("hot_pepper", 0x1F336)
    put("potato", 0x1F954)
    put("yam", 0x1F360)
    put("chestnut", 0x1F330)
    put("peanuts", 0x1F95C)
    put("honey", 0x1F36F)
    put("croissant", 0x1F950)
    put("bread", 0x1F35E)
    put("baguette", 0x1F956)
    put("cheese", 0x1F9C0)
    put("egg", 0x1F95A)
    put("cooking", 0x1F373)
    put("bacon", 0x1F953)
    put("pancakes", 0x1F95E)
    put("tempura", 0x1F364)
    put("drumstick", 0x1F357)
    put("meat", 0x1F356)
    put("pizza", 0x1F355)
    put("hotdog", 0x1F32D)
    put("hamburger", 0x1F354)
    put("fries", 0x1F35F)
    put("doner_kebab", 0x1F959)
    put("taco", 0x1F32E)
    put("burrito", 0x1F32F)
    put("salad", 0x1F957)
    put("paella", 0x1F958)
    put("spaghetti", 0x1F35D)
    put("ramen", 0x1F35C)
    put("food", 0x1F372)
    put("naruto", 0x1F365)
    put("sushi", 0x1F363)
    put("bento", 0x1F371)
    put("curry", 0x1F35B)
    put("rice", 0x1F35A)
    put("onigiri", 0x1F359)
    put("senbei", 0x1F358)
    put("oden", 0x1F362)
    put("dango", 0x1F361)
    put("shaved_ice", 0x1F367)
    put("ice_cream", 0x1F368)
    put("soft_serve", 0x1F366)
    put("cake", 0x1F370)
    put("birthday", 0x1F382)
    put("custard", 0x1F36E)
    put("lollipop", 0x1F36D)
    put("candy", 0x1F36C)
    put("chocolate", 0x1F36B)
    put("popcorn", 0x1F37F)
    put("donut", 0x1F369)
    put("cookie", 0x1F36A)
    put("milk", 0x1F95B)
    put("baby_bottle", 0x1F37C)
    put("coffee", 0x2615)
    put("tea", 0x1F375)
    put("sake", 0x1F376)
    put("beer", 0x1F37A)
    put("beers", 0x1F37B)
    put("clink", 0x1F942)
    put("wine", 0x1F377)
    put("small_glass", 0x1F943)
    put("cocktail", 0x1F378)
    put("tropical_drink", 0x1F379)
    put("champagne", 0x1F37E)
    put("spoon", 0x1F944)
    put("fork_and_knife", 0x1F374)
    put("hungry", 0x1F37D)
    put("football", 0x26BD)
    put("basketball", 0x1F3C0)
    put("american_football", 0x1F3C8)
    put("baseball", 0x26BE)
    put("tennis", 0x1F3BE)
    put("volleyball", 0x1F3D0)
    put("rugby", 0x1F3C9)
    put("billiards", 0x1F3B1)
    put("ping_pong", 0x1F3D3)
    put("badminton", 0x1F3F8)
    put("gooooooooal", 0x1F945)
    put("ice_hockey", 0x1F3D2)
    put("field_hockey", 0x1F3D1)
    put("cricket", 0x1F3CF)
    put("hole_in_one", 0x26F3)
    put("bow_and_arrow", 0x1F3F9)
    put("fishing", 0x1F3A3)
    put("boxing_glove", 0x1F94A)
    put("black_belt", 0x1F94B)
    put("ice_skate", 0x26F8)
    put("ski", 0x1F3BF)
    put("skier", 0x26F7)
    put("snowboarder", 0x1F3C2)
    put("lift", 0x1F3CB)
    put("fencing", 0x1F93A)
    put("wrestling", 0x1F93C)
    put("cartwheel", 0x1F938)
    put("ball", 0x26F9)
    put("handball", 0x1F93E)
    put("golf", 0x1F3CC)
    put("surf", 0x1F3C4)
    put("swim", 0x1F3CA)
    put("water_polo", 0x1F93D)
    put("rowboat", 0x1F6A3)
    put("horse_racing", 0x1F3C7)
    put("cyclist", 0x1F6B4)
    put("mountain_biker", 0x1F6B5)
    put("running_shirt", 0x1F3BD)
    put("medal", 0x1F3C5)
    put("military_medal", 0x1F396)
    put("first_place", 0x1F947)
    put("second_place", 0x1F948)
    put("third_place", 0x1F949)
    put("trophy", 0x1F3C6)
    put("rosette", 0x1F3F5)
    put("reminder_ribbon", 0x1F397)
    put("pass", 0x1F3AB)
    put("ticket", 0x1F39F)
    put("circus", 0x1F3AA)
    put("juggling", 0x1F939)
    put("performing_arts", 0x1F3AD)
    put("art", 0x1F3A8)
    put("action", 0x1F3AC)
    put("microphone", 0x1F3A4)
    put("headphones", 0x1F3A7)
    put("musical_score", 0x1F3BC)
    put("piano", 0x1F3B9)
    put("drum", 0x1F941)
    put("saxophone", 0x1F3B7)
    put("trumpet", 0x1F3BA)
    put("guitar", 0x1F3B8)
    put("violin", 0x1F3BB)
    put("dice", 0x1F3B2)
    put("direct_hit", 0x1F3AF)
    put("strike", 0x1F3B3)
    put("video_game", 0x1F3AE)
    put("slot_machine", 0x1F3B0)
    put("car", 0x1F697)
    put("taxi", 0x1F695)
    put("recreational_vehicle", 0x1F699)
    put("bus", 0x1F68C)
    put("trolley", 0x1F68E)
    put("racecar", 0x1F3CE)
    put("police_car", 0x1F693)
    put("ambulance", 0x1F691)
    put("fire_truck", 0x1F692)
    put("minibus", 0x1F690)
    put("moving_truck", 0x1F69A)
    put("truck", 0x1F69B)
    put("tractor", 0x1F69C)
    put("kick_scooter", 0x1F6F4)
    put("bike", 0x1F6B2)
    put("scooter", 0x1F6F5)
    put("motorcycle", 0x1F3CD)
    put("siren", 0x1F6A8)
    put("oncoming_police_car", 0x1F694)
    put("oncoming_bus", 0x1F68D)
    put("oncoming_car", 0x1F698)
    put("oncoming_taxi", 0x1F696)
    put("aerial_tramway", 0x1F6A1)
    put("gondola", 0x1F6A0)
    put("suspension_railway", 0x1F69F)
    put("railway_car", 0x1F683)
    put("tram", 0x1F68B)
    put("mountain_railway", 0x1F69E)
    put("monorail", 0x1F69D)
    put("high_speed_train", 0x1F684)
    put("bullet_train", 0x1F685)
    put("light_rail", 0x1F688)
    put("train", 0x1F682)
    put("oncoming_train", 0x1F686)
    put("subway", 0x1F687)
    put("oncoming_tram", 0x1F68A)
    put("station", 0x1F689)
    put("helicopter", 0x1F681)
    put("small_airplane", 0x1F6E9)
    put("airplane", 0x2708)
    put("take_off", 0x1F6EB)
    put("landing", 0x1F6EC)
    put("rocket", 0x1F680)
    put("satellite", 0x1F6F0)
    put("seat", 0x1F4BA)
    put("canoe", 0x1F6F6)
    put("boat", 0x26F5)
    put("motor_boat", 0x1F6E5)
    put("speedboat", 0x1F6A4)
    put("passenger_ship", 0x1F6F3)
    put("ferry", 0x26F4)
    put("ship", 0x1F6A2)
    put("anchor", 0x2693)
    put("work_in_progress", 0x1F6A7)
    put("fuel_pump", 0x26FD)
    put("bus_stop", 0x1F68F)
    put("traffic_light", 0x1F6A6)
    put("horizontal_traffic_light", 0x1F6A5)
    put("map", 0x1F5FA)
    put("rock_carving", 0x1F5FF)
    put("statue", 0x1F5FD)
    put("fountain", 0x26F2)
    put("tower", 0x1F5FC)
    put("castle", 0x1F3F0)
    put("shiro", 0x1F3EF)
    put("stadium", 0x1F3DF)
    put("ferris_wheel", 0x1F3A1)
    put("roller_coaster", 0x1F3A2)
    put("carousel", 0x1F3A0)
    put("beach_umbrella", 0x26F1)
    put("beach", 0x1F3D6)
    put("island", 0x1F3DD)
    put("mountain", 0x26F0)
    put("snowy_mountain", 0x1F3D4)
    put("mount_fuji", 0x1F5FB)
    put("volcano", 0x1F30B)
    put("desert", 0x1F3DC)
    put("campsite", 0x1F3D5)
    put("tent", 0x26FA)
    put("railway_track", 0x1F6E4)
    put("road", 0x1F6E3)
    put("construction", 0x1F3D7)
    put("factory", 0x1F3ED)
    put("house", 0x1F3E0)
    put("suburb", 0x1F3E1)
    put("houses", 0x1F3D8)
    put("derelict_house", 0x1F3DA)
    put("office", 0x1F3E2)
    put("department_store", 0x1F3EC)
    put("japan_post", 0x1F3E3)
    put("post_office", 0x1F3E4)
    put("hospital", 0x1F3E5)
    put("bank", 0x1F3E6)
    put("hotel", 0x1F3E8)
    put("convenience_store", 0x1F3EA)
    put("school", 0x1F3EB)
    put("love_hotel", 0x1F3E9)
    put("wedding", 0x1F492)
    put("classical_building", 0x1F3DB)
    put("church", 0x26EA)
    put("mosque", 0x1F54C)
    put("synagogue", 0x1F54D)
    put("kaaba", 0x1F54B)
    put("shinto_shrine", 0x26E9)
    put("japan", 0x1F5FE)
    put("moon_ceremony", 0x1F391)
    put("national_park", 0x1F3DE)
    put("sunrise", 0x1F305)
    put("mountain_sunrise", 0x1F304)
    put("shooting_star", 0x1F320)
    put("sparkler", 0x1F387)
    put("fireworks", 0x1F386)
    put("city_sunrise", 0x1F307)
    put("sunset", 0x1F306)
    put("city", 0x1F3D9)
    put("night", 0x1F303)
    put("milky_way", 0x1F30C)
    put("bridge", 0x1F309)
    put("foggy", 0x1F301)
    put("watch", 0x231A)
    put("mobile_phone", 0x1F4F1)
    put("calling", 0x1F4F2)
    put("computer", 0x1F4BB)
    put("keyboard", 0x2328)
    put("desktop_computer", 0x1F5A5)
    put("printer", 0x1F5A8)
    put("computer_mouse", 0x1F5B1)
    put("trackball", 0x1F5B2)
    put("joystick", 0x1F579)
    put("compression", 0x1F5DC)
    put("gold_record", 0x1F4BD)
    put("floppy_disk", 0x1F4BE)
    put("cd", 0x1F4BF)
    put("dvd", 0x1F4C0)
    put("vhs", 0x1F4FC)
    put("camera", 0x1F4F7)
    put("taking_a_picture", 0x1F4F8)
    put("video_camera", 0x1F4F9)
    put("movie_camera", 0x1F3A5)
    put("projector", 0x1F4FD)
    put("film", 0x1F39E)
    put("landline", 0x1F4DE)
    put("phone", 0x260E)
    put("pager", 0x1F4DF)
    put("fax", 0x1F4E0)
    put("tv", 0x1F4FA)
    put("radio", 0x1F4FB)
    put("studio_microphone", 0x1F399)
    put("volume", 0x1F39A)
    put("control_knobs", 0x1F39B)
    put("stopwatch", 0x23F1)
    put("timer", 0x23F2)
    put("alarm_clock", 0x23F0)
    put("mantelpiece_clock", 0x1F570)
    put("times_up", 0x231B)
    put("time_ticking", 0x23F3)
    put("satellite_antenna", 0x1F4E1)
    put("battery", 0x1F50B)
    put("electric_plug", 0x1F50C)
    put("light_bulb", 0x1F4A1)
    put("flashlight", 0x1F526)
    put("candle", 0x1F56F)
    put("wastebasket", 0x1F5D1)
    put("oil_drum", 0x1F6E2)
    put("losing_money", 0x1F4B8)
    put("dollar_bills", 0x1F4B5)
    put("yen_banknotes", 0x1F4B4)
    put("euro_banknotes", 0x1F4B6)
    put("pound_notes", 0x1F4B7)
    put("money", 0x1F4B0)
    put("credit_card", 0x1F4B3)
    put("gem", 0x1F48E)
    put("justice", 0x2696)
    put("fixing", 0x1F527)
    put("hammer", 0x1F528)
    put("at_work", 0x2692)
    put("working_on_it", 0x1F6E0)
    put("mine", 0x26CF)
    put("nut_and_bolt", 0x1F529)
    put("gear", 0x2699)
    put("chains", 0x26D3)
    put("gun", 0x1F52B)
    put("bomb", 0x1F4A3)
    put("knife", 0x1F52A)
    put("dagger", 0x1F5E1)
    put("duel", 0x2694)
    put("shield", 0x1F6E1)
    put("smoking", 0x1F6AC)
    put("coffin", 0x26B0)
    put("funeral_urn", 0x26B1)
    put("vase", 0x1F3FA)
    put("crystal_ball", 0x1F52E)
    put("prayer_beads", 0x1F4FF)
    put("barber", 0x1F488)
    put("alchemy", 0x2697)
    put("telescope", 0x1F52D)
    put("science", 0x1F52C)
    put("hole", 0x1F573)
    put("medicine", 0x1F48A)
    put("injection", 0x1F489)
    put("temperature", 0x1F321)
    put("toilet", 0x1F6BD)
    put("potable_water", 0x1F6B0)
    put("shower", 0x1F6BF)
    put("bathtub", 0x1F6C1)
    put("bath", 0x1F6C0)
    put("bellhop_bell", 0x1F6CE)
    put("key", 0x1F511)
    put("secret", 0x1F5DD)
    put("door", 0x1F6AA)
    put("living_room", 0x1F6CB)
    put("bed", 0x1F6CF)
    put("in_bed", 0x1F6CC)
    put("picture", 0x1F5BC)
    put("shopping_bags", 0x1F6CD)
    put("shopping_cart", 0x1F6D2)
    put("gift", 0x1F381)
    put("balloon", 0x1F388)
    put("carp_streamer", 0x1F38F)
    put("ribbon", 0x1F380)
    put("confetti", 0x1F38A)
    put("tada", 0x1F389)
    put("dolls", 0x1F38E)
    put("lantern", 0x1F3EE)
    put("wind_chime", 0x1F390)
    put("email", 0x2709)
    put("mail_sent", 0x1F4E9)
    put("mail_received", 0x1F4E8)
    put("e-mail", 0x1F4E7)
    put("love_letter", 0x1F48C)
    put("inbox", 0x1F4E5)
    put("outbox", 0x1F4E4)
    put("package", 0x1F4E6)
    put("label", 0x1F3F7)
    put("closed_mailbox", 0x1F4EA)
    put("mailbox", 0x1F4EB)
    put("unread_mail", 0x1F4EC)
    put("inbox_zero", 0x1F4ED)
    put("mail_dropoff", 0x1F4EE)
    put("horn", 0x1F4EF)
    put("scroll", 0x1F4DC)
    put("receipt", 0x1F4C3)
    put("document", 0x1F4C4)
    put("place_holder", 0x1F4D1)
    put("bar_chart", 0x1F4CA)
    put("chart", 0x1F4C8)
    put("downwards_trend", 0x1F4C9)
    put("spiral_notepad", 0x1F5D2)
    put("date", 0x1F4C6)
    put("calendar", 0x1F4C5)
    put("rolodex", 0x1F4C7)
    put("archive", 0x1F5C3)
    put("ballot_box", 0x1F5F3)
    put("file_cabinet", 0x1F5C4)
    put("clipboard", 0x1F4CB)
    put("organize", 0x1F4C1)
    put("folder", 0x1F4C2)
    put("sort", 0x1F5C2)
    put("newspaper", 0x1F5DE)
    put("headlines", 0x1F4F0)
    put("notebook", 0x1F4D3)
    put("decorative_notebook", 0x1F4D4)
    put("ledger", 0x1F4D2)
    put("red_book", 0x1F4D5)
    put("green_book", 0x1F4D7)
    put("blue_book", 0x1F4D8)
    put("orange_book", 0x1F4D9)
    put("books", 0x1F4DA)
    put("book", 0x1F4D6)
    put("bookmark", 0x1F516)
    put("link", 0x1F517)
    put("paperclip", 0x1F4CE)
    put("office_supplies", 0x1F587)
    put("carpenter_square", 0x1F4D0)
    put("ruler", 0x1F4CF)
    put("push_pin", 0x1F4CC)
    put("pin", 0x1F4CD)
    put("scissors", 0x2702)
    put("pen", 0x1F58A)
    put("fountain_pen", 0x1F58B)
    put("paintbrush", 0x1F58C)
    put("crayon", 0x1F58D)
    put("memo", 0x1F4DD)
    put("pencil", 0x270F)
    put("search", 0x1F50D)
    put("privacy", 0x1F50F)
    put("secure", 0x1F510)
    put("locked", 0x1F512)
    put("unlocked", 0x1F513)
    put("heart", 0x2764)
    put("yellow_heart", 0x1F49B)
    put("green_heart", 0x1F49A)
    put("blue_heart", 0x1F499)
    put("purple_heart", 0x1F49C)
    put("black_heart", 0x1F5A4)
    put("broken_heart", 0x1F494)
    put("heart_exclamation", 0x2763)
    put("two_hearts", 0x1F495)
    put("revolving_hearts", 0x1F49E)
    put("heartbeat", 0x1F493)
    put("heart_pulse", 0x1F497)
    put("sparkling_heart", 0x1F496)
    put("cupid", 0x1F498)
    put("gift_heart", 0x1F49D)
    put("heart_box", 0x1F49F)
    put("peace", 0x262E)
    put("cross", 0x271D)
    put("star_and_crescent", 0x262A)
    put("om", 0x1F549)
    put("wheel_of_dharma", 0x2638)
    put("star_of_david", 0x2721)
    put("menorah", 0x1F54E)
    put("yin_yang", 0x262F)
    put("orthodox_cross", 0x2626)
    put("place_of_worship", 0x1F6D0)
    put("ophiuchus", 0x26CE)
    put("aries", 0x2648)
    put("taurus", 0x2649)
    put("gemini", 0x264A)
    put("cancer", 0x264B)
    put("leo", 0x264C)
    put("virgo", 0x264D)
    put("libra", 0x264E)
    put("scorpius", 0x264F)
    put("sagittarius", 0x2650)
    put("capricorn", 0x2651)
    put("aquarius", 0x2652)
    put("pisces", 0x2653)
    put("id", 0x1F194)
    put("atom", 0x269B)
    put("radioactive", 0x2622)
    put("biohazard", 0x2623)
    put("phone_off", 0x1F4F4)
    put("vibration_mode", 0x1F4F3)
    put("japanese_not_free_of_charge_button", 0x1F236)
    put("japanese_bargain_button", 0x1F250)
    put("japanese_acceptable_button", 0x1F251)
    put("japanese_free_of_charge_button", 0x1F21A)
    put("japanese_application_button", 0x1F238)
    put("japanese_open_for_business_button", 0x1F23A)
    put("japanese_monthly_amount_button", 0x1F237)
    put("japanese_secret_button", 0x3299)
    put("japanese_congratulations_button", 0x3297)
    put("japanese_passing_grade_button", 0x1F234)
    put("japanese_no_vacancy_button", 0x1F235)
    put("japanese_discount_button", 0x1F239)
    put("japanese_prohibited_button", 0x1F232)
    put("eight_pointed_star", 0x2734)
    put("vs", 0x1F19A)
    put("white_flower", 0x1F4AE)
    put("a", 0x1F170)
    put("b", 0x1F171)
    put("ab", 0x1F18E)
    put("cl", 0x1F191)
    put("o", 0x1F17E)
    put("sos", 0x1F198)
    put("cross_mark", 0x274C)
    put("circle", 0x2B55)
    put("stop_sign", 0x1F6D1)
    put("no_entry", 0x26D4)
    put("name_badge", 0x1F4DB)
    put("prohibited", 0x1F6AB)
    put("100", 0x1F4AF)
    put("anger", 0x1F4A2)
    put("hot_springs", 0x2668)
    put("no_pedestrians", 0x1F6B7)
    put("do_not_litter", 0x1F6AF)
    put("no_bicycles", 0x1F6B3)
    put("non-potable_water", 0x1F6B1)
    put("underage", 0x1F51E)
    put("no_phones", 0x1F4F5)
    put("no_smoking", 0x1F6AD)
    put("exclamation", 0x2757)
    put("grey_exclamation", 0x2755)
    put("question", 0x2753)
    put("grey_question", 0x2754)
    put("bangbang", 0x203C)
    put("interrobang", 0x2049)
    put("low_brightness", 0x1F505)
    put("brightness", 0x1F506)
    put("part_alternation", 0x303D)
    put("warning", 0x26A0)
    put("children_crossing", 0x1F6B8)
    put("trident", 0x1F531)
    put("fleur_de_lis", 0x269C)
    put("beginner", 0x1F530)
    put("recycle", 0x267B)
    put("check", 0x2705)
    put("stock_market", 0x1F4B9)
    put("sparkle", 0x2747)
    put("eight_spoked_asterisk", 0x2733)
    put("x", 0x274E)
    put("www", 0x1F310)
    put("cute", 0x1F4A0)
    put("metro", 0x24C2)
    put("cyclone", 0x1F300)
    put("zzz", 0x1F4A4)
    put("atm", 0x1F3E7)
    put("wc", 0x1F6BE)
    put("accessible", 0x267F)
    put("parking", 0x1F17F)
    put("passport_control", 0x1F6C2)
    put("customs", 0x1F6C3)
    put("baggage_claim", 0x1F6C4)
    put("locker", 0x1F6C5)
    put("mens", 0x1F6B9)
    put("womens", 0x1F6BA)
    put("baby_change_station", 0x1F6BC)
    put("restroom", 0x1F6BB)
    put("put_litter_in_its_place", 0x1F6AE)
    put("cinema", 0x1F3A6)
    put("cell_reception", 0x1F4F6)
    put("symbols", 0x1F523)
    put("info", 0x2139)
    put("abc", 0x1F524)
    put("abcd", 0x1F521)
    put("capital_abcd", 0x1F520)
    put("ng", 0x1F196)
    put("squared_ok", 0x1F197)
    put("squared_up", 0x1F199)
    put("cool", 0x1F192)
    put("new", 0x1F195)
    put("free", 0x1F193)
//            put("zero", 0x0030 - 20E3)
//            put("one", 0x0031 - 20E3)
//            put("two", 0x0032 - 20E3)
//            put("three", 0x0033 - 20E3)
//            put("four", 0x0034 - 20E3)
//            put("five", 0x0035 - 20E3)
//            put("six", 0x0036 - 20E3)
//            put("seven", 0x0037 - 20E3)
//            put("eight", 0x0038 - 20E3)
//            put("nine", 0x0039 - 20E3)
    put("ten", 0x1F51F)
    put("1234", 0x1F522)
//            put("hash", 0x0023 - 20E3)
//            put("asterisk", 0x002A - 20E3)
    put("play", 0x25B6)
    put("pause", 0x23F8)
    put("play_pause", 0x23EF)
    put("stop_button", 0x23F9)
    put("record", 0x23FA)
    put("next_track", 0x23ED)
    put("previous_track", 0x23EE)
    put("fast_forward", 0x23E9)
    put("rewind", 0x23EA)
    put("double_up", 0x23EB)
    put("double_down", 0x23EC)
    put("play_reverse", 0x25C0)
    put("upvote", 0x1F53C)
    put("downvote", 0x1F53D)
    put("right", 0x27A1)
    put("left", 0x2B05)
    put("up", 0x2B06)
    put("down", 0x2B07)
    put("upper_right", 0x2197)
    put("lower_right", 0x2198)
    put("lower_left", 0x2199)
    put("upper_left", 0x2196)
    put("up_down", 0x2195)
    put("left_right", 0x2194)
    put("forward", 0x21AA)
    put("reply", 0x21A9)
    put("heading_up", 0x2934)
    put("heading_down", 0x2935)
    put("shuffle", 0x1F500)
    put("repeat", 0x1F501)
    put("repeat_one", 0x1F502)
    put("counterclockwise", 0x1F504)
    put("clockwise", 0x1F503)
    put("music", 0x1F3B5)
    put("musical_notes", 0x1F3B6)
    put("plus", 0x2795)
    put("minus", 0x2796)
    put("division", 0x2797)
    put("multiplication", 0x2716)
    put("dollars", 0x1F4B2)
    put("exchange", 0x1F4B1)
    put("tm", 0x2122)
    put("wavy_dash", 0x3030)
    put("loop", 0x27B0)
    put("double_loop", 0x27BF)
    put("end", 0x1F51A)
    put("back", 0x1F519)
    put("on", 0x1F51B)
    put("top", 0x1F51D)
    put("soon", 0x1F51C)
    put("check_mark", 0x2714)
    put("checkbox", 0x2611)
    put("radio_button", 0x1F518)
    put("white_circle", 0x26AA)
    put("black_circle", 0x26AB)
    put("red_circle", 0x1F534)
    put("blue_circle", 0x1F535)
    put("red_triangle_up", 0x1F53A)
    put("red_triangle_down", 0x1F53B)
    put("small_orange_diamond", 0x1F538)
    put("small_blue_diamond", 0x1F539)
    put("large_orange_diamond", 0x1F536)
    put("large_blue_diamond", 0x1F537)
    put("black_and_white_square", 0x1F533)
    put("white_and_black_square", 0x1F532)
    put("black_small_square", 0x25AA)
    put("white_small_square", 0x25AB)
    put("black_medium_small_square", 0x25FE)
    put("white_medium_small_square", 0x25FD)
    put("black_medium_square", 0x25FC)
    put("white_medium_square", 0x25FB)
    put("black_large_square", 0x2B1B)
    put("white_large_square", 0x2B1C)
    put("yellow_large_square", 0x1F7E8)
    put("green_large_square", 0x1F7E9)
    put("speaker", 0x1F508)
    put("mute", 0x1F507)
    put("softer", 0x1F509)
    put("louder", 0x1F50A)
    put("notifications", 0x1F514)
    put("mute_notifications", 0x1F515)
    put("megaphone", 0x1F4E3)
    put("loudspeaker", 0x1F4E2)
    put("umm", 0x1F4AC)
    put("speech_bubble", 0x1F5E8)
    put("thought", 0x1F4AD)
    put("anger_bubble", 0x1F5EF)
    put("spades", 0x2660)
    put("clubs", 0x2663)
    put("hearts", 0x2665)
    put("diamonds", 0x2666)
    put("joker", 0x1F0CF)
    put("playing_cards", 0x1F3B4)
    put("mahjong", 0x1F004)
    put("time", 0x1F557)
    put("white_flag", 0x1F3F3)
    put("black_flag", 0x1F3F4)
    put("checkered_flag", 0x1F3C1)
    put("triangular_flag", 0x1F6A9)
    put("crossed_flags", 0x1F38C)
}

