package com.example.data.datasource

import com.example.data.model.PlaceCategory
import com.example.data.model.ZarayenPlace

object ZarayenPlacesData {
    val places = listOf(
        // 🏛️ مەڵبەند و پارکە گشتییەکان
        ZarayenPlace(
            id = "gov_1",
            name = "مەڵبەندی ناحیەی زەڕایەن (Zarayen)",
            category = PlaceCategory.PARK_GOV,
            address = "ناوەندی شارۆچکەی زەڕایەن، شەقامی سەرەکی",
            description = "مەڵبەند و بەڕێوەبەرایەتی فەرمی ناحیەی زەڕایەن لە قەزای شارەزوور",
            googleMapsUrl = "https://maps.app.goo.gl/VnyXna76pJKSCxyw6"
        ),
        ZarayenPlace(
            id = "park_1",
            name = "پارکی زەڕایەن (Zarayan park)",
            category = PlaceCategory.PARK_GOV,
            address = "دەروازەی شارۆچکەی زەڕایەن",
            description = "پارکی گشتی و ژینگەیی زەڕایەن بۆ پشوو و گەشت و کات بەسەربردنی خێزانی",
            googleMapsUrl = "https://maps.google.com/?q=Zarayan+park"
        ),

        // 🕌 مزگەوتەکان
        ZarayenPlace(
            id = "mosque_1",
            name = "مزگەوتی گەورەی زەڕایەن",
            category = PlaceCategory.MOSQUES,
            address = "ناوەندی بازاڕی زەڕایەن",
            description = "گەورەترین مزگەوتی شارۆچکەی زەڕایەن بۆ نوێژی هەینی و نوێژە بەکۆمەڵەکان و مەراسیمی پرسە",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+گەورەی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_2",
            name = "مزگەوتی مەولەوی",
            category = PlaceCategory.MOSQUES,
            address = "گەڕەکی مەولەوی، زەڕایەن",
            description = "مزگەوتی مەولەوی بەناوی شاعیری گەورەی کورد، ناوەندی عیبادەت و وتار",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+مەولەوی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_3",
            name = "مزگەوتی کانی ئاسکان",
            category = PlaceCategory.MOSQUES,
            address = "گەڕەکی کانی ئاسکان، زەڕایەن",
            description = "مزگەوتێکی دیاری گەڕەکی کانی ئاسکان لە زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+کانی+ئاسکان+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_4",
            name = "مزگەوتی شێخ ساحێب",
            category = PlaceCategory.MOSQUES,
            address = "گەڕەکی شێخ ساحێب، زەڕایەن",
            description = "مزگەوتی شێخ ساحێب بۆ نوێژ و وانە ئایینییەکان",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+شێخ+ساحێب+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_5",
            name = "مزگەوتی سەلاحەدین",
            category = PlaceCategory.MOSQUES,
            address = "شەقامی سەلاحەدین، زەڕایەن",
            description = "مزگەوتی سەلاحەدینی ئەیوبی لە شارۆچکەی زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+سەلاحەدین+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_6",
            name = "مزگەوتی پیر خدری شاهۆ",
            category = PlaceCategory.MOSQUES,
            address = "ناوچەی ڕۆژهەڵاتی زەڕایەن",
            description = "مزگەوتی پیر خدری شاهۆ بە پێگەی دێرین و ئایینی",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+پیر+خدری+شاهۆ+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_7",
            name = "مزگەوتی دەروازە",
            category = PlaceCategory.MOSQUES,
            address = "سەرەتای دەروازەی شارۆچکەی زەڕایەن",
            description = "مزگەوتی دەروازەی زەڕایەن پێشوازی لە ڕێبواران و دانیشتووانی شارۆچکەکە دەکات",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+دەروازە+زەڕایەن"
        ),
        ZarayenPlace(
            id = "mosque_8",
            name = "مزگەوتی مەحوی",
            category = PlaceCategory.MOSQUES,
            address = "گەڕەکی مەحوی، زەڕایەن",
            description = "مزگەوتی مەحوی ناوەندی نوێژ و وانەی قورئانخوێنی",
            googleMapsUrl = "https://maps.google.com/?q=مزگەوتی+مەحوی+زەڕایەن"
        ),

        // 🏫 قوتابخانە و پەروەردەییەکان
        ZarayenPlace(
            id = "school_1",
            name = "قوتابخانەی هەوارانی بنەڕەتی",
            category = PlaceCategory.SCHOOLS,
            address = "گەڕەکی هەواران، زەڕایەن",
            description = "قوتابخانەی هەوارانی بنەڕەتی بۆ فێرخوازانی قۆناغی سەرەتایی",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+هەواران+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_2",
            name = "قوتابخانەی کاروانی بنەڕەتی",
            category = PlaceCategory.SCHOOLS,
            address = "گەڕەکی کاروان، زەڕایەن",
            description = "قوتابخانەی کاروانی بنەڕەتی خزمەتگوزاری پەروەردەیی پێشکەش دەکات",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+کاروان+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_3",
            name = "قوتابخانەی دانای بنەڕەتی",
            category = PlaceCategory.SCHOOLS,
            address = "گەڕەکی دانا، زەڕایەن",
            description = "قوتابخانەی دانای بنەڕەتی بۆ قۆناغی بنەڕەتی",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+دانا+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_4",
            name = "قوتابخانەی کانی میل بنەڕەتی",
            category = PlaceCategory.SCHOOLS,
            address = "ناوچەی کانی میل، زەڕایەن",
            description = "قوتابخانەی کانی میل بۆ فێرکردن و پەروەردەی نەوەکان",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+کانی+میل+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_5",
            name = "قوتابخانەی کوردۆی بنەڕەتی (٧-٩)",
            category = PlaceCategory.SCHOOLS,
            address = "شەقامی پەروەردە، زەڕایەن",
            description = "قوتابخانەی کوردۆی بنەڕەتی بۆ پۆلەکانی حەوت تا نۆیەم (ناوەندی)",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+کوردۆ+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_6",
            name = "قوتابخانەی شەهید مامۆستا ستاری بنەڕەتی (٧-٩)",
            category = PlaceCategory.SCHOOLS,
            address = "نزیک مەڵبەندی ناحیە، زەڕایەن",
            description = "قوتابخانەی شەهید مامۆستا ستار بۆ پۆلەکانی حەوتەم تا نۆیەم",
            googleMapsUrl = "https://maps.google.com/?q=قوتابخانەی+شەهید+مامۆستا+ستار+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_7",
            name = "ئامادەیی نیشتیمانی کچان",
            category = PlaceCategory.SCHOOLS,
            address = "شەقامی قوتابخانەکان، زەڕایەن",
            description = "ئامادەیی نیشتیمانی کچان بۆ خوێندنی ئامادەیی زانستی و وێژەیی",
            googleMapsUrl = "https://maps.google.com/?q=ئامادەیی+نیشتیمانی+کچان+زەڕایەن"
        ),
        ZarayenPlace(
            id = "school_8",
            name = "ئامادەیی شەهید سەرچڵی کوڕان",
            category = PlaceCategory.SCHOOLS,
            address = "گەڕەکی ئامادەیی، زەڕایەن",
            description = "ئامادەیی شەهید سەرچڵ بۆ قۆناغی ئامادەیی کوڕان (زانستی و وێژەیی)",
            googleMapsUrl = "https://maps.google.com/?q=ئامادەیی+شەهید+سەرچڵ+زەڕایەن"
        ),

        // 🏥 کەرتی تەندروستی و خزمەتگوزاریی گشتی
        ZarayenPlace(
            id = "health_1",
            name = "مەڵبەندی تەندروستی شەهید چیا",
            category = PlaceCategory.HEALTH,
            address = "شەقامی سەرەکی تەندروستی، زەڕایەن",
            description = "مەڵبەندی سەرەکی تەندروستی زەڕایەن بۆ فریاگوزاری و خزمەتگوزاری پزیشکی و دەرمانخانە",
            phone = "0770XXXXXXX",
            googleMapsUrl = "https://maps.google.com/?q=مەڵبەندی+تەندروستی+شەهید+چیا+زەڕایەن"
        ),
        ZarayenPlace(
            id = "gov_2",
            name = "بەڕێوەبەرایەتی ناحیەی زەڕایەن",
            category = PlaceCategory.HEALTH,
            address = "ناوەندی شارۆچکەی زەڕایەن",
            description = "فەرمانگەی بەڕێوەبەرایەتی کاروباری کارگێڕی و خزمەتگوزارییەکانی ناحیەی زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=ناحیەی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "gov_3",
            name = "فەرمانگەی ئاوی زەڕایەن",
            category = PlaceCategory.HEALTH,
            address = "زەڕایەن",
            description = "فەرمانگەی دابەشکردن و بەڕێوەبردنی تۆڕی ئاوی خاوێنی شارۆچکەی زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=فەرمانگەی+ئاوی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "gov_4",
            name = "بنکەی پۆلیسی زەڕایەن",
            category = PlaceCategory.HEALTH,
            address = "نزیک دەروازەی زەڕایەن",
            description = "بنکەی پاراستنی ئاسایش و هێمنی و جێبەجێکردنی یاسا لە زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=بنکەی+پۆلیسی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "gov_5",
            name = "بنکەی بەرگری شارستانی (ئاگرکوژێنەوە)",
            category = PlaceCategory.HEALTH,
            address = "شەقامی گشتی زەڕایەن",
            description = "تیمی ئاگرکوژێنەوە و فریاکەوتنی خێرا بۆ ڕووداوە لەناکاوەکان",
            googleMapsUrl = "https://maps.google.com/?q=بەرگری+شارستانی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "gov_6",
            name = "بنکەی هاتووچۆی زەڕایەن",
            category = PlaceCategory.HEALTH,
            address = "شەقامی سەرەکی، زەڕایەن",
            description = "ڕێکخستنی هاتووچۆ و شەقامەکانی ناو و دەوروبەری شارۆچکەی زەڕایەن",
            googleMapsUrl = "https://maps.google.com/?q=هاتووچۆی+زەڕایەن"
        ),

        // ⚽ کەرتی وەرزش
        ZarayenPlace(
            id = "sport_1",
            name = "یاریگای کوردستان",
            category = PlaceCategory.SPORTS,
            address = "ناوچەی وەرزشی زەڕایەن",
            description = "یاریگای تۆپی پێی فەرمی بۆ یارییە بەکۆمەڵەکان و خولی شارۆچکە",
            googleMapsUrl = "https://maps.google.com/?q=یاریگای+کوردستان+زەڕایەن"
        ),
        ZarayenPlace(
            id = "sport_2",
            name = "یاریگای شەهید ئەنوەر",
            category = PlaceCategory.SPORTS,
            address = "زەڕایەن",
            description = "یاریگای شەهید ئەنوەر بۆ ڕاهێنان و چالاکییە وەرزشییەکان",
            googleMapsUrl = "https://maps.google.com/?q=یاریگای+شەهید+ئەنوەر+زەڕایەن"
        ),
        ZarayenPlace(
            id = "sport_3",
            name = "یاریگای تارتانی مەولەوی",
            category = PlaceCategory.SPORTS,
            address = "گەڕەکی مەولەوی، زەڕایەن",
            description = "یاریگای تارتانی چیمەنی دەستکرد بۆ یاری شەوانە و گرێبەست",
            googleMapsUrl = "https://maps.google.com/?q=یاریگای+تارتانی+مەولەوی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "sport_4",
            name = "یاریگای تارتانی پێشمەرگە",
            category = PlaceCategory.SPORTS,
            address = "زەڕایەن",
            description = "یاریگای مۆدێرنی تارتان بۆ لاوان و وەرزشوانان",
            googleMapsUrl = "https://maps.google.com/?q=یاریگای+تارتانی+پێشمەرگە+زەڕایەن"
        ),
        ZarayenPlace(
            id = "sport_5",
            name = "هۆڵی لەشجوانی زەڕایەن (Zarayan gym)",
            category = PlaceCategory.SPORTS,
            address = "ناوەندی زەڕایەن",
            description = "هۆڵی مۆدێرنی لەشجوانی و فیتنێس بە ئامێری پێشکەوتوو بۆ ڕاهێنان",
            googleMapsUrl = "https://maps.google.com/?q=Zarayan+gym"
        ),

        // 🛒 مارکێت، دوکان و بازاڕەکان
        ZarayenPlace(
            id = "market_1",
            name = "مارکێتی حاجی (Haji Market)",
            category = PlaceCategory.MARKETS,
            address = "شەقامی سەرەکی بازاڕی زەڕایەن",
            description = "مارکێتی گشتگیر بۆ پێداویستی ڕۆژانەی ماڵ، خۆراک و پاککەرەوە",
            googleMapsUrl = "https://maps.google.com/?q=Haji+Market+Zarayan"
        ),
        ZarayenPlace(
            id = "market_2",
            name = "مارکێتی ڕامان",
            category = PlaceCategory.MARKETS,
            address = "بازاڕی زەڕایەن",
            description = "مارکێتی ڕامان بۆ کەلوپەلی خۆراکی و پێداویستی خێزانی",
            googleMapsUrl = "https://maps.google.com/?q=مارکێتی+ڕامان+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_3",
            name = "مارکێتی نوسەر",
            category = PlaceCategory.MARKETS,
            address = "نزیک قوتابخانەکان، زەڕایەن",
            description = "کتێبخانە و پێداویستی خوێندنگە و پەڕاوگە لەگەڵ کەلوپەلی گشتی",
            googleMapsUrl = "https://maps.google.com/?q=مارکێتی+نوسەر+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_4",
            name = "مارکێتی شارەزوور",
            category = PlaceCategory.MARKETS,
            address = "سەر شەقامی گشتی، زەڕایەن",
            description = "سوپەرمارکێتی گەورەی شارەزوور بۆ سەرجەم کاڵاکان",
            googleMapsUrl = "https://maps.google.com/?q=مارکێتی+شارەزوور+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_5",
            name = "پۆشاکی ژیکاڵ (جلوبەرگی خێزان و منداڵان)",
            category = PlaceCategory.MARKETS,
            address = "بازاڕی ناوەندی زەڕایەن",
            description = "پێشانگای مۆدێرنی جلوبەرگ و پۆشاکی منداڵان و خێزان بە کوالێتی بەرز",
            googleMapsUrl = "https://maps.google.com/?q=پۆشاکی+ژیکاڵ+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_6",
            name = "نانەواخانە و سەمونخانەکانی زەڕایەن",
            category = PlaceCategory.MARKETS,
            address = "گەڕەک و بازاڕی زەڕایەن",
            description = "نان و سەمونی گەرم و تەندروست بە بەردەوامی ڕۆژانە",
            googleMapsUrl = "https://maps.google.com/?q=نانەواخانە+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_7",
            name = "میوەفرۆشی و گۆشتفرۆشییەکان",
            category = PlaceCategory.MARKETS,
            address = "شەقامی سەرەکی بازاڕ، زەڕایەن",
            description = "گۆشتی تازەی خۆماڵی و میوە و سەوزەی بەردەوامی بازاڕ",
            googleMapsUrl = "https://maps.google.com/?q=بازاڕی+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_8",
            name = "دوکانەکانی تەکنەلۆژیا و چاککردنەوەی مۆبایل",
            category = PlaceCategory.MARKETS,
            address = "شەقامی بازاڕ، زەڕایەن",
            description = "فرۆشتن و چاککردنەوەی مۆبایل و ئامێرە ئەلیکترۆنییەکان و کارتی ئینتەرنێت",
            googleMapsUrl = "https://maps.google.com/?q=مۆبایل+زەڕایەن"
        ),
        ZarayenPlace(
            id = "market_9",
            name = "پیشەسازی و بیناسازی و فیتەری زەڕایەن",
            category = PlaceCategory.MARKETS,
            address = "ناوچەی پیشەسازی زەڕایەن",
            description = "وەرشەکانی چاککردنەوەی ئۆتۆمبێل، فیتەری، و کەلوپەلی بیناسازی و ناوماڵ",
            googleMapsUrl = "https://maps.google.com/?q=پیشەسازی+زەڕایەن"
        ),

        // 💵 دارایی و سووتەمەنی
        ZarayenPlace(
            id = "fin_1",
            name = "نوسینگەی ئاڵوگۆڕی دراو و حەواڵەی بەمۆ (بەمۆ ئێکسپرێس)",
            category = PlaceCategory.FINANCE_FUEL,
            address = "بازاڕی دراوی زەڕایەن",
            description = "گۆڕینەوەی دراوە بیانییەکان بە نرخی ڕۆژ و حەواڵەکردنی خێرا بۆ هەموو ناوچەکان",
            googleMapsUrl = "https://maps.google.com/?q=ئاڵوگۆڕی+دراو+زەڕایەن"
        ),
        ZarayenPlace(
            id = "fuel_1",
            name = "وێستگەی سووتەمەنی باز (بەنزینخانەی باز)",
            category = PlaceCategory.FINANCE_FUEL,
            address = "شەقامی سەرەکی سلێمانی - زەڕایەن",
            description = "سووتەمەنی و بەنزینی نۆرماڵ، موحەسەن و سوپەر بە کوالێتی بەرز",
            googleMapsUrl = "https://maps.google.com/?q=بەنزینخانەی+باز+زەڕایەن"
        ),
        ZarayenPlace(
            id = "fuel_2",
            name = "وێستگەی سووتەمەنی سەرهەنگ",
            category = PlaceCategory.FINANCE_FUEL,
            address = "سەر ڕێگای سەرەکی زەڕایەن",
            description = "دابینکردنی سووتەمەنی، غاز و ڕۆنی ئۆتۆمبێل",
            googleMapsUrl = "https://maps.google.com/?q=بەنزینخانەی+سەرهەنگ+زەڕایەن"
        ),
        ZarayenPlace(
            id = "fuel_3",
            name = "وێستگەی سووتەمەنی وارماوا",
            category = PlaceCategory.FINANCE_FUEL,
            address = "نزیک سنوری وارماوا - زەڕایەن",
            description = "خزمەتگوزاری سووتەمەنی بۆ شۆفێران و هاووڵاتیان",
            googleMapsUrl = "https://maps.google.com/?q=وێستگەی+وارماوا"
        )
    )
}
