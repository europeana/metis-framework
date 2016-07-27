/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.api.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to work with <a href="http://openrdf.org">Sesame</a> .
 * 
 * @author Borys Omelayenko
 * 
 */
public class Language {

	/**
	 * Language codes.
	 */
	public static enum Lang {
		aa("aa", "aar", "Afar"), ab("ab", "abk", "Abkhazian"), ae("ae", "ave",
				"Avestan"), af("af", "afr", "Afrikaans"), ak("ak", "aka",
				"Akan"), am("am", "amh", "Amharic"), an("an", "arg",
				"Aragonese"), ar("ar", "ara", "Arabic"), as("as", "asm",
				"Assamese"), av("av", "ava", "Avaric"), ay("ay", "aym",
				"Aymara"), az("az", "aze", "Azerbaijani"), ba("ba", "bak",
				"Bashkir"), be("be", "bel", "Belarusian"), bg("bg", "bul",
				"Bulgarian"), bh("bh", "bih", "Bihari"), bi("bi", "bis",
				"Bislama"), bm("bm", "bam", "Bambara"), bn("bn", "ben",
				"Bengali"), bo("bo", "tib", "Tibetan"), br("br", "bre",
				"Breton"), bs("bs", "bos", "Bosnian"), ca("ca", "cat",
				"Catalan"), ce("ce", "che", "Chechen"), ch("ch", "cha",
				"Chamorro"), co("co", "cos", "Corsican"), cr("cr", "cre",
				"Cree"), cs("cs", "cze", "Czech"), cu("cu", "chu",
				"Church Slavic"), cv("cv", "chv", "Chuvash"), cy("cy", "wel",
				"Welsh"), da("da", "dan", "Danish"), de("de", "ger", "German"), dv(
				"dv", "div", "Divehi"), dz("dz", "dzo", "Dzongkha"), ee("ee",
				"ewe", "Ewe"), el("el", "gre", "Greek"), en("en", "eng",
				"English"), eo("eo", "epo", "Esperanto"), es("es", "spa",
				"Spanish"), et("et", "est", "Estonian"), eu("eu", "baq",
				"Basque"), fa("fa", "per", "Persian"), ff("ff", "ful", "Fulah"), fi(
				"fi", "fin", "Finnish"), fj("fj", "fij", "Fijian"), fo("fo",
				"fao", "Faroese"), fr("fr", "fre", "French"), fy("fy", "fry",
				"Western Frisian"), ga("ga", "gle", "Irish"), gd("gd", "gla",
				"Scottish Gaelic"), gl("gl", "glg", "Galician"), gn("gn",
				"grn", "Guarani"), gu("gu", "guj", "Gujarati"), gv("gv", "glv",
				"Manx"), ha("ha", "hau", "Hausa"), he("he", "heb", "Hebrew"), hi(
				"hi", "hin", "Hindi"), ho("ho", "hmo", "Hiri Motu"), hr("hr",
				"hrv", "Croatian"), ht("ht", "hat", "Haitian"), hu("hu", "hun",
				"Hungarian"), hy("hy", "arm", "Armenian"), hz("hz", "her",
				"Herero"), ia("ia", "ina",
				"Interlingua (International Auxiliary Language Association)"), id(
				"id", "ind", "Indonesian"), ie("ie", "ile", "Interlingue"), ig(
				"ig", "ibo", "Igbo"), ii("ii", "iii", "Sichuan Yi"), ik("ik",
				"ipk", "Inupiaq"), io("io", "ido", "Ido"), is("is", "ice",
				"Icelandic"), it("it", "ita", "Italian"), iu("iu", "iku",
				"Inuktitut"), ja("ja", "jpn", "Japanese"), jv("jv", "jav",
				"Javanese"), ka("ka", "geo", "Georgian"), kg("kg", "kon",
				"Kongo"), ki("ki", "kik", "Kikuyu"), kj("kj", "kua", "Kwanyama"), kk(
				"kk", "kaz", "Kazakh"), kl("kl", "kal", "Kalaallisut"), km(
				"km", "khm", "Khmer"), kn("kn", "kan", "Kannada"), ko("ko",
				"kor", "Korean"), kr("kr", "kau", "Kanuri"), ks("ks", "kas",
				"Kashmiri"), ku("ku", "kur", "Kurdish"), kv("kv", "kom", "Komi"), kw(
				"kw", "cor", "Cornish"), ky("ky", "kir", "Kirghiz"), la("la",
				"lat", "Latin"), lb("lb", "ltz", "Luxembourgish"), lg("lg",
				"lug", "Ganda"), li("li", "lim", "Limburgish"), ln("ln", "lin",
				"Lingala"), lo("lo", "lao", "Lao"), lt("lt", "lit",
				"Lithuanian"), lu("lu", "lub", "Luba-Katanga"), lv("lv", "lav",
				"Latvian"), mg("mg", "mlg", "Malagasy"), mh("mh", "mah",
				"Marshallese"), mi("mi", "mao", "Maori"), mk("mk", "mkd",
				"Macedonian"), ml("ml", "mal", "Malayalam"), mn("mn", "mon",
				"Mongolian"), mo("mo", "mol", "Moldavian"), mr("mr", "mar",
				"Marathi"), ms("ms", "may", "Malay"), mt("mt", "mlt", "Maltese"), my(
				"my", "bur", "Burmese"), na("na", "nau", "Nauru"), nb("nb",
				"nob", "Norwegian Bokmal"), nd("nd", "nde", "North Ndebele"), ne(
				"ne", "nep", "Nepali"), ng("ng", "ndo", "Ndonga"), nl("nl",
				"nld", "Dutch"), nn("nn", "nno", "Norwegian Nynorsk"), no("no",
				"nor", "Norwegian"), nr("nr", "nbl", "South Ndebele"), nv("nv",
				"nav", "Navajo"), ny("ny", "nya", "Chichewa"), oc("oc", "oci",
				"Occitan"), oj("oj", "oji", "Ojibwa"), om("om", "orm", "Oromo"), or(
				"or", "ori", "Oriya"), os("os", "oss", "Ossetian"), pa("pa",
				"pan", "Panjabi"), pi("pi", "pli", "Pali"), pl("pl", "pol",
				"Polish"), ps("ps", "pus", "Pashto"), pt("pt", "por",
				"Portuguese"), qu("qu", "que", "Quechua"), rm("rm", "roh",
				"Raeto-Romance"), rn("rn", "run", "Kirundi"), ro("ro", "ron",
				"Romanian"), ru("ru", "rus", "Russian"), rw("rw", "kin",
				"Kinyarwanda"), sa("sa", "san", "Sanskrit"), sc("sc", "srd",
				"Sardinian"), sd("sd", "snd", "Sindhi"), se("se", "sme",
				"Northern Sami"), sg("sg", "sag", "Sango"), sh("sh", null,
				"Serbo-Croatian"), si("si", "sin", "Sinhalese"), sk("sk",
				"slo", "Slovak"), sl("sl", "slv", "Slovenian"), sm("sm", "smo",
				"Samoan"), sn("sn", "sna", "Shona"), so("so", "som", "Somali"), sq(
				"sq", "alb", "Albanian"), sr("sr", "scc", "Serbian"), ss("ss",
				"ssw", "Swati"), st("st", "sot", "Sotho"), su("su", "sun",
				"Sundanese"), sv("sv", "swe", "Swedish"), sw("sw", "swa",
				"Swahili"), ta("ta", "tam", "Tamil"), te("te", "tel", "Telugu"), tg(
				"tg", "tgk", "Tajik"), th("th", "tha", "Thai"), ti("ti", "tir",
				"Tigrinya"), tk("tk", "tuk", "Turkmen"), tl("tl", "tgl",
				"Tagalog"), tn("tn", "tsn", "Tswana"), to("to", "ton", "Tonga"), tr(
				"tr", "tur", "Turkish"), ts("ts", "tso", "Tsonga"), tt("tt",
				"tat", "Tatar"), tw("tw", "twi", "Twi"), ty("ty", "tah",
				"Tahitian"), ug("ug", "uig", "Uighur"), uk("uk", "ukr",
				"Ukrainian"), ur("ur", "urd", "Urdu"), uz("uz", "uzb", "Uzbek"), ve(
				"ve", "ven", "Venda"), vi("vi", "vie", "Vietnamese"), vo("vo",
				"vol", "Volapuk"), wa("wa", "wln", "Walloon"), wo("wo", "wol",
				"Wolof"), xh("xh", "xho", "Xhosa"), yi("yi", "yid", "Yiddish"), yo(
				"yo", "yor", "Yoruba"), za("za", "zha", "Zhuang"), zh("zh",
				"chi", "Chinese"), zu("zu", "zul", "Zulu");

		private String code;

		Lang(String code639_1, String code639_2, String name) {
			if (code639_1.length() != 2)
				throw new RuntimeException(
						"Lang code should be of two letters: " + code);
			this.code = code639_1;
		}

		@Override
		public String toString() {
			return code;
		}

		public String getCode() {
			return code;
		}

		static Map<String, String> codeCorrection = new HashMap<String, String>();
		static {
			codeCorrection.put("ua", "uk");
			codeCorrection.put("gr", "el");
			codeCorrection.put("sp", "es");
			codeCorrection.put("jp", "ja");
			codeCorrection.put("ge", "de");
		}

		/**
		 * Finds a Lang constant given the language code.
		 * 
		 * @param langCode
		 */
		public static Lang parseLang(String langCode) throws Exception {
			if (langCode == null)
				return null;
			if (langCode.length() != 2)
				return null;
			if (codeCorrection.containsKey(langCode)) {
				langCode = codeCorrection.get(langCode);
			}
			return Lang.valueOf(langCode);
		}
	}
}