package com.example.data.generator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object StarterQuranPdfGenerator {

    fun generateStarterMushaf(context: Context): File {
        val dir = File(context.filesDir, "mushafs")
        if (!dir.exists()) dir.mkdirs()

        val targetFile = File(dir, "mushaf_al_madinah_starter.pdf")
        if (targetFile.exists() && targetFile.length() > 5000) {
            return targetFile
        }

        val doc = PdfDocument()
        val pageWidth = 595 // standard A4 pt
        val pageHeight = 842

        val pagesContent = listOf(
            PageInfo(
                surahTitle = "المصحف الشريف",
                juzText = "مقدمة",
                pageNumber = 1,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "الْقُرْآنُ الْكَرِيمُ",
                    "مُصْحَفُ الْمَدِينَةِ النَّبَوِيَّةِ",
                    "بِرِوَايَةِ حَفْصٍ عَنْ عَاصِمٍ",
                    "بِالرَّسْمِ الْعُثْمَانِيِّ الشَّرِيفِ",
                    "",
                    "«إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ»",
                    "«وَيُبَشِّرُ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا كَبِيرًا»"
                ),
                isCover = true
            ),
            PageInfo(
                surahTitle = "فهرس السور المباركة",
                juzText = "الفهرس",
                pageNumber = 2,
                lines = listOf(
                    "فِهْرِسُ السُّوَرِ وَالْأَجْزَاءِ الْمُخْتَارَةِ",
                    "------------------------------------------",
                    "١. سُورَةُ الْفَاتِحَةِ ..................... ص ٣",
                    "٢. سُورَةُ الْبَقَرَةِ (أَوَائِلُ السُّورَةِ) .... ص ٤",
                    "٣. آيَةُ الْكُرْسِيِّ وَخَوَاتِيمُ الْبَقَرَةِ .... ص ٥",
                    "٤. سُورَةُ الْكَهْفِ (نُورُ الْجُمُعَةِ) ..... ص ٦",
                    "٥. سُورَةُ يس (قَلْبُ الْقُرْآنِ) ......... ص ٧",
                    "٦. سُورَةُ الْمُلْكِ (الْمُنْجِيَةُ) .......... ص ٨",
                    "٧. قِصَارُ السُّوَرِ (الضُّحَى إِلَى الْقَدْرِ) .. ص ٩",
                    "٨. الْمُعَوِّذَتَانِ وَالْإِخْلَاصُ ............ ص ١٠",
                    "٩. دُعَاءُ خَتْمِ الْقُرْآنِ الْعَظِيمِ ..... ص ١١",
                    "",
                    "قَالَ رَسُولُ اللَّهِ ﷺ:",
                    "«خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ»"
                )
            ),
            PageInfo(
                surahTitle = "سُورَةُ الْفَاتِحَةِ",
                juzText = "الجزء الأول",
                pageNumber = 3,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾",
                    "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾",
                    "الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾",
                    "مَالِكِ يَوْمِ الدِّينِ ﴿٤﴾",
                    "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٥﴾",
                    "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٦﴾",
                    "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ",
                    "غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴿٧﴾",
                    "آمِين"
                )
            ),
            PageInfo(
                surahTitle = "سُورَةُ الْبَقَرَةِ",
                juzText = "الجزء الأول",
                pageNumber = 4,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "الم ﴿١﴾ ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ ﴿٢﴾",
                    "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ",
                    "وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ ﴿٣﴾",
                    "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ",
                    "وَبِالْآخِرَةِ هُمْ يُوقِنُونَ ﴿٤﴾",
                    "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ ﴿٥﴾",
                    "",
                    "إِنَّ الَّذِينَ كَفَرُوا سَوَاءٌ عَلَيْهِمْ أَأَنذَرْتَهُمْ",
                    "أَمْ لَمْ تُنذِرْهُمْ لَا يُؤْمِنُونَ ﴿٦﴾"
                )
            ),
            PageInfo(
                surahTitle = "آيَةُ الْكُرْسِيِّ",
                juzText = "الجزء الثالث",
                pageNumber = 5,
                lines = listOf(
                    "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ",
                    "لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ",
                    "لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ",
                    "مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ",
                    "يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ",
                    "وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ",
                    "وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ",
                    "وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ ﴿٢٥٥﴾",
                    "",
                    "«آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ...»"
                )
            ),
            PageInfo(
                surahTitle = "سُورَةُ الْكَهْفِ",
                juzText = "الجزء الخامس عشر",
                pageNumber = 6,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ",
                    "وَلَمْ يَجْعَل لَّهُ عِوَجًا ۜ ﴿١﴾",
                    "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ",
                    "وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ",
                    "أَنَّ لَهُمْ أَجْرًا حَسَنًا ﴿٢﴾ مَّاكِثِينَ فِيهِ أَبَدًا ﴿٣﴾",
                    "وَيُنذِرَ الَّذِينَ قَالُوا اتَّخَذَ اللَّهُ وَلَدًا ﴿٤﴾",
                    "مَّا لَهُم بِهِ مِنْ عِلْمٍ وَلَا لِآبَائِهِمْ ۚ كَبُرَتْ كَلِمَةً تَخْرُجُ مِنْ أَفْوَاهِهِمْ ۚ",
                    "إِن يَقُولُونَ إِلَّا كَذِبًا ﴿٥﴾"
                )
            ),
            PageInfo(
                surahTitle = "سُورَةُ يس",
                juzText = "الجزء الثاني والعشرون",
                pageNumber = 7,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "يس ﴿١﴾ وَالْقُرْآنِ الْحَكِيمِ ﴿٢﴾",
                    "إِنَّكَ لَمِنَ الْمُرْسَلِينَ ﴿٣﴾ عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ ﴿٤﴾",
                    "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ ﴿٥﴾",
                    "لِتُنذِرَ قَوْمًا مَّا أُنذِرَ آبَاؤُهُمْ فَهُمْ غَافِلُونَ ﴿٦﴾",
                    "لَقَدْ حَقَّ الْقَوْلُ عَلَىٰ أَكْثَرِهِمْ فَهُمْ لَا يُؤْمِنُونَ ﴿٧﴾",
                    "إِنَّا جَعَلْنَا فِي أَعْنَاقِهِمْ أَغْلَالًا فَهِيَ إِلَى الْأَذْقَانِ فَهُم مُّقْمَحُونَ ﴿٨﴾",
                    "وَجَعَلْنَا مِن بَيْنِ أَيْدِيهِمْ سَدًّا وَمِنْ خَلْفِهِمْ سَدًّا",
                    "فَأَغْشَيْنَاهُمْ فَهُمْ لَا يُبْصِرُونَ ﴿٩﴾"
                )
            ),
            PageInfo(
                surahTitle = "سُورَةُ الْمُلْكِ",
                juzText = "الجزء التاسع والعشرون",
                pageNumber = 8,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ﴿١﴾",
                    "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ",
                    "وَهُوَ الْعَزِيزُ الْغَفُورُ ﴿٢﴾",
                    "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ",
                    "فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ ﴿٣﴾",
                    "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا",
                    "وَهُوَ حَسِيرٌ ﴿٤﴾"
                )
            ),
            PageInfo(
                surahTitle = "قِصَارُ السُّوَرِ",
                juzText = "الجزء الثلاثون",
                pageNumber = 9,
                lines = listOf(
                    "سُورَةُ الضُّحَى",
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "وَالضُّحَىٰ ﴿١﴾ وَاللَّيْلِ إِذَا سَجَىٰ ﴿٢﴾ مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ ﴿٣﴾",
                    "وَلَلْآخِرَةُ خَيْرٌ لَّكَ مِنَ الْأُولَىٰ ﴿٤﴾ وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ ﴿٥﴾",
                    "أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ ﴿٦﴾ وَوَجَدَكَ ضَالًّا فَهَدَىٰ ﴿٧﴾",
                    "وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ ﴿٨﴾ فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ ﴿٩﴾",
                    "وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ ﴿١٠﴾ وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ ﴿١١﴾"
                )
            ),
            PageInfo(
                surahTitle = "الْمُعَوِّذَتَانِ وَالْإِخْلَاصُ",
                juzText = "الجزء الثلاثون",
                pageNumber = 10,
                lines = listOf(
                    "سُورَةُ الْإِخْلَاصِ",
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "قُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾",
                    "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾",
                    "",
                    "سُورَةُ الْفَلَقِ",
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾",
                    "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾",
                    "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾",
                    "",
                    "سُورَةُ النَّاسِ",
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَٰهِ النَّاسِ ﴿٣﴾",
                    "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾",
                    "مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾"
                )
            ),
            PageInfo(
                surahTitle = "دُعَاءُ خَتْمِ الْقُرْآنِ",
                juzText = "دعاء الختم",
                pageNumber = 11,
                lines = listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    "اللَّهُمَّ ارْحَمْنَا بِالْقُرْآنِ الْعَظِيمِ،",
                    "وَاجْعَلْهُ لَنَا إِمَامًا وَنُورًا وَهُدًى وَرَحْمَةً.",
                    "",
                    "اللَّهُمَّ ذَكِّرْنَا مِنْهُ مَا نَسِينَا، وَعَلِّمْنَا مِنْهُ مَا جَهِلْنَا،",
                    "وَارْزُقْنَا تِلَاوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ،",
                    "وَاجْعَلْهُ لَنَا حُجَّةً يَا رَبَّ الْعَالَمِينَ.",
                    "",
                    "اللَّهُمَّ أَصْلِحْ لَنَا دِينَنَا الَّذِي هُوَ عِصْمَةُ أَمْرِنَا،",
                    "وَأَصْلِحْ لَنَا دُنْيَانَا الَّتِي فِيهَا مَعَاشُنَا،",
                    "وَأَصْلِحْ لَنَا آخِرَتَنَا الَّتِي إِلَيْهَا مَعَادُنَا.",
                    "",
                    "وَصَلَّى اللَّهُ وَسَلَّمَ عَلَى نَبِيِّنَا مُحَمَّدٍ وَعَلَى آلِهِ وَصَحْبِهِ أَجْمَعِينَ."
                )
            )
        )

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        for ((index, item) in pagesContent.withIndex()) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create())
            val canvas = page.canvas
            drawQuranPage(canvas, pageWidth.toFloat(), pageHeight.toFloat(), item)
            doc.finishPage(page)
        }

        FileOutputStream(targetFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return targetFile
    }

    private fun drawQuranPage(canvas: Canvas, w: Float, h: Float, item: PageInfo) {
        // Background - subtle parchment tint
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FCF9F2")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Ornate borders
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor("#B78B35") // Gold
            strokeWidth = 3.5f
            style = Paint.Style.STROKE
        }
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#0E6655") // Islamic green
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        canvas.drawRect(RectF(24f, 24f, w - 24f, h - 24f), outerBorderPaint)
        canvas.drawRect(RectF(30f, 30f, w - 30f, h - 30f), innerBorderPaint)

        // Decorative corners
        val cornerPaint = Paint().apply {
            color = Color.parseColor("#B78B35")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(30f, 30f, 4f, cornerPaint)
        canvas.drawCircle(w - 30f, 30f, 4f, cornerPaint)
        canvas.drawCircle(30f, h - 30f, 4f, cornerPaint)
        canvas.drawCircle(w - 30f, h - 30f, 4f, cornerPaint)

        // Header Surah Banner
        if (!item.isCover) {
            val bannerPaint = Paint().apply {
                color = Color.parseColor("#EAE3D2")
                style = Paint.Style.FILL
            }
            val bannerStroke = Paint().apply {
                color = Color.parseColor("#B78B35")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val bannerRect = RectF(50f, 44f, w - 50f, 84f)
            canvas.drawRoundRect(bannerRect, 8f, 8f, bannerPaint)
            canvas.drawRoundRect(bannerRect, 8f, 8f, bannerStroke)

            val headerTextPaint = Paint().apply {
                color = Color.parseColor("#0E6655")
                textSize = 17f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(item.surahTitle, w / 2f, 69f, headerTextPaint)

            // Juz info left/right
            val juzPaint = Paint().apply {
                color = Color.parseColor("#7D6608")
                textSize = 11f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            canvas.drawText(item.juzText, 55f, 40f, juzPaint)
        }

        // Body Text
        val bodyTextPaint = Paint().apply {
            color = Color.parseColor("#17202A")
            textSize = if (item.isCover) 18f else 15f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, if (item.isCover) Typeface.BOLD else Typeface.NORMAL)
            isAntiAlias = true
        }

        val startY = if (item.isCover) 240f else 125f
        val lineSpacing = if (item.isCover) 38f else 32f

        var currentY = startY
        for (line in item.lines) {
            if (line.contains("بِسْمِ اللَّهِ") || line.contains("سُورَةُ")) {
                bodyTextPaint.color = Color.parseColor("#0E6655")
                bodyTextPaint.textSize = 17f
                bodyTextPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            } else if (line.contains("﴿") || line.contains("آيَةُ")) {
                bodyTextPaint.color = Color.parseColor("#1B2631")
                bodyTextPaint.textSize = 14.5f
                bodyTextPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            } else {
                bodyTextPaint.color = Color.parseColor("#2C3E50")
                bodyTextPaint.textSize = 14f
                bodyTextPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            }

            canvas.drawText(line, w / 2f, currentY, bodyTextPaint)
            currentY += lineSpacing
        }

        // Footer Page Number
        val footerPaint = Paint().apply {
            color = Color.parseColor("#B78B35")
            textSize = 12f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val footerBoxPaint = Paint().apply {
            color = Color.parseColor("#F5EEF8")
            style = Paint.Style.FILL
        }
        val footerRect = RectF(w / 2f - 24f, h - 50f, w / 2f + 24f, h - 32f)
        canvas.drawRoundRect(footerRect, 6f, 6f, footerBoxPaint)
        canvas.drawText("﴿ ${item.pageNumber} ﴾", w / 2f, h - 36f, footerPaint)
    }

    private data class PageInfo(
        val surahTitle: String,
        val juzText: String,
        val pageNumber: Int,
        val lines: List<String>,
        val isCover: Boolean = false
    )
}
