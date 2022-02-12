package com.example.myfirstapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.example.myfirstapp.ml.Food101Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder.nativeOrder
import java.util.*
import kotlin.math.roundToInt

class Prediction: AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var predictedImage: ImageView
    private lateinit var predictName: TextView
    private lateinit var prob: TextView
    private lateinit var cameraBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prediction_layout)

        predictedImage = findViewById(R.id.predictedImage)
        predictName = findViewById(R.id.predictionName)
        prob = findViewById(R.id.prob)
        cameraBtn = findViewById(R.id.cameraBtn)
        title = findViewById(R.id.predictTitle)

        // making a predict
        predict()

        // back to camera
        cameraBtn.setOnClickListener {
            finish()
        }

    }

    private fun predict() {
        // prepare image
        predictedImage.setImageURI(Uri.parse(intent.getStringExtra("image")))
        val drawable = predictedImage.drawable as BitmapDrawable
        var image = drawable.bitmap
        var dimension = image.width.coerceAtMost(image.height)
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
        image = Bitmap.createScaledBitmap(image, 224, 224, false)


        // predict the image
        val model = Food101Model.newInstance(applicationContext)

        var inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        // create buffer

        var intValues = IntArray(224 * 224)
        var inputList: MutableList<Int> = mutableListOf<Int>()
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
        for (i: Int in 0 until 224) {
            for (j: Int in 0 until 224) {
                var value = intValues[pixel++]
                inputList.add(((value.shr(16)) and 0xFF))
                inputList.add(((value.shr(8)) and 0xFF))
                inputList.add((value and 0xFF))
            }
        }

        inputFeature0.loadArray(inputList.toIntArray())

        // predictions
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        val classNames = arrayOf("apple_pie", "baby_back_ribs", "baklava", "beef_carpaccio", "beef_tartare", "beet_salad", "beignets", "bibimbap", "bread_pudding", "breakfast_burrito", "bruschetta", "caesar_salad", "cannoli", "caprese_salad", "carrot_cake", "ceviche", "cheesecake", "cheese_plate", "chicken_curry", "chicken_quesadilla", "chicken_wings", "chocolate_cake", "chocolate_mousse", "churros", "clam_chowder", "club_sandwich", "crab_cakes", "creme_brulee", "croque_madame", "cup_cakes", "deviled_eggs", "donuts", "dumplings", "edamame", "eggs_benedict", "escargots", "falafel", "filet_mignon", "fish_and_chips", "foie_gras", "french_fries", "french_onion_soup", "french_toast", "fried_calamari", "fried_rice", "frozen_yogurt", "garlic_bread", "gnocchi", "greek_salad", "grilled_cheese_sandwich", "grilled_salmon", "guacamole", "gyoza", "hamburger", "hot_and_sour_soup", "hot_dog", "huevos_rancheros", "hummus", "ice_cream", "lasagna", "lobster_bisque", "lobster_roll_sandwich", "macaroni_and_cheese", "macarons", "miso_soup", "mussels", "nachos", "omelette", "onion_rings", "oysters", "pad_thai", "paella", "pancakes", "panna_cotta", "peking_duck", "pho", "pizza", "pork_chop", "poutine", "prime_rib", "pulled_pork_sandwich", "ramen", "ravioli", "red_velvet_cake", "risotto", "samosa", "sashimi", "scallops", "seaweed_salad", "shrimp_and_grits", "spaghetti_bolognese", "spaghetti_carbonara", "spring_rolls", "steak", "strawberry_shortcake", "sushi", "tacos", "takoyaki", "tiramisu", "tuna_tartare", "waffles")
        val classNamesChinese = arrayOf("蘋果派", "小排骨", "巴卡拉", "生牛肉", "牛肉韃靼", "甜菜沙拉", "貝涅", "拌飯", "麵包布丁", "早餐捲餅", "布魯斯凱塔", "凱撒沙拉", "香炸奶酪卷", "卡普雷塞沙拉", "胡蘿蔔蛋糕", "酸橘汁醃魚", "芝士蛋糕", "奶酪盤", "咖哩雞", "雞肉玉米餅", "雞翅", "巧克力蛋糕", "巧克力慕斯", "油條", "蛤蜊雜燴", "俱樂部三明治", "蟹餅", "焦糖布丁", "法式夫人", "紙杯蛋糕", "魔鬼蛋", "甜甜圈", "水餃", "毛豆", "雞蛋本尼迪克特", "蝸牛", "沙拉三明治", "烤里脊肉片", "魚和薯條", "鵝肝", "炸薯條", "法式洋蔥湯", "法式吐司", "炸魷魚", "炒飯", "冰凍酸奶", "大蒜麵包", "湯糰", "希臘式沙拉", "烤奶酪三明治", "烤三文魚", "鱷梨", "餃子", "漢堡包", "酸辣湯", "熱狗", "墨西哥煎蛋", "鷹嘴豆泥", "冰淇淋", "烤寬麵條", "龍蝦濃湯", "龍蝦卷三明治", "通心粉和奶酪", "馬卡龍", "味噌湯", "青口貝", "玉米片", "煎蛋餅", "洋蔥圈", "生蠔", "泰式炒河粉", "西班牙海鮮飯", "薄煎餅", "意式奶凍", "北京烤鴨", "河粉", "比薩", "豬排", "布丁", "牛排", "拉豬肉三明治", "拉麵", "餛飩", "紅色天鵝絨蛋糕", "燴飯", "薩摩薩", "生魚片", "扇貝", "海藻沙拉", "蝦和粉打窩沙食", "肉醬意粉", "意粉培根蛋麵", "春捲", "牛扒", "草莓脆餅", "壽司", "炸玉米餅", "章魚燒", "提拉米蘇", "金槍魚韃靼", "鬆餅" )

        // output results
        val classNamesProbs: MutableMap<String, Float> = mutableMapOf(classNames[0] to confidences[0])
        val classNamesProbsChinese: MutableMap<String, Float> = mutableMapOf(classNamesChinese[0] to confidences[0])
        for (i: Int in 1 until 101) {
            classNamesProbs[classNames[i]] = confidences[i]
            classNamesProbsChinese[classNamesChinese[i]] = confidences[i]
        }

        val result = classNamesProbs.toList().sortedBy { (_, value) -> value }.toMap()
        val resultChinese = classNamesProbsChinese.toList().sortedBy { (_, value) -> value }.toMap()
        val length = result.size

        if (intent.getStringExtra("lan") == "chinese") {
            title.text = "預測"
            predictName.text =  "${resultChinese.keys.toList()[length - 1]}\n${resultChinese.keys.toList()[length - 2]}\n${resultChinese.keys.toList()[length - 3]}"
            cameraBtn.text = "相機"
        }else{
            title.text = "Prediction"
            predictName.text = "${result.keys.toList()[length - 1]}\n${result.keys.toList()[length - 2]}\n${result.keys.toList()[length - 3]}"
            cameraBtn.text = "CAMERA"
        }
        prob.text = ("${(result.values.toList()[length - 1] * 10000.0).roundToInt() / 100.0}%\n${(result.values.toList()[length - 2] * 10000.0).roundToInt() / 100.0}%\n${(result.values.toList()[length - 3] * 10000.0).roundToInt() / 100.0}%")

        model.close()
    }
}