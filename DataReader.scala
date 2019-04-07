import scala.collection.mutable.Map

object DataReader {
  def read_file_lines(filename: String): Array[String] = {
    val src = io.Source.fromFile(filename)
    try src.mkString.split("\n") finally src.close()
  }

  def get_body_lines(input: Array[String], start_pat: String, end_pat: String) = {
    val mstart = start_pat.r
    val mend = end_pat.r
    var start = -1 // use Option[Int] = Option.empty instead ?
    var end = -1   // use Option[Int] = Option.empty instead ?
    input.zipWithIndex.foreach {
      case (mstart(_*), i) => start = i
      case (mend(_*), i) => end = i
      case _ =>
    }
    input.slice(start, end)
  }

  def get_purchases(data: Array[String]) = {
    val purchase_pattern =   """\s*([\.0-9]+)\s+([\.0-9]+)\s+([_0-9]+)\s+(\S+)\s+("[^"]+")(\s+#.*)?""".r
    val sec_map = Map[String, List[(Double, Double)]]()
    for (line <- data) {
      line match {
        case purchase_pattern(price, pieces, _, _, security, _) => {
          val currList = sec_map.getOrElse(security, List[(Double, Double)]())
          sec_map += (security -> ((price.toDouble, pieces.toDouble) :: currList))
        }
        case _ => println(s"not match for line $line")
      }
    }
    sec_map
  }

  def get_references(input: Array[String]) = {
    val date_match = """\s*([_0-9]+)""".r
    val date = input(0) match {
      case date_match(thedate) => thedate
      case _ => ""
    }
    val references = Map[String, (String, Double)]()
    for (line <- input.tail) {
      val ref_res = format_reference_line(line)
      references += (ref_res._1 -> (date, ref_res._2))
    }
    references
  }

  def format_reference_line(line: String) = {
    val match0 = """\s*("[^"]+")\s+([\.0-9]+)(\s+.*)?""".r
    line match {
      case match0(name, price, _) => (name, price.toDouble)
      case _ => ("", 0.0)
    }
  }

  def make_full_report(sec_map:    Map[String, List[(Double, Double)]],
                       references: Map[String, (String, Double)],
                       fmt:        String) = {
    import scala.collection.mutable.MutableList // :-(
    var diffs = MutableList[Double]()
    var fullReport = MutableList[String]()
    for (key <- sec_map.keys) {
      val secReport = make_security_report(key, sec_map, references)
      val diff = security_value(sec_map(key), references(key)._2) - security_principal(sec_map(key))
      fullReport += fmt.format(key, secReport._1, secReport._2, secReport._3)
      diffs += diff
    }

    val abs_diffs = diffs.map(x => Math.abs(x))
    val abs_pf_diff = abs_diffs.reduce((x,y) => x + y)
    for (i <- 0 until abs_diffs.length) {
      val frac = 100 * abs_diffs(i) / abs_pf_diff
      fullReport(i) = fullReport(i) + " (%5.2f%s of total swing)".format(frac, "%")
    }

    val pfReport = make_portfolio_report(sec_map, references, fmt)
    fullReport.sortWith(_ < _) :+ "-----------------------------------" :+
      fmt.format("Portfolio", pfReport._1, pfReport._2, pfReport._3)
  }

  def make_security_report(sec_name:   String,
                           sec_map:    Map[String, List[(Double, Double)]],
                           references: Map[String, (String, Double)]) = {
    val sec_price = references(sec_name)._2
    val sec_principal = security_principal(sec_map(sec_name))
    val sec_val = security_value(sec_map(sec_name), sec_price)
    val diff = sec_val - sec_principal
    val percent_gain = 100 * diff / sec_principal
    (sec_val, percent_gain, "%")
  }

  def security_principal(purchases: List[(Double, Double)]) = {
    var sec_principal = 0.0
    for (purchase <- purchases)
      sec_principal += purchase._1 * purchase._2
    sec_principal
  }

  def security_value(purchases: List[(Double, Double)], price: Double) = {
    var sec_val = 0.0
    for (purchase <- purchases)
      sec_val += purchase._2
    sec_val * price
  }

  def make_portfolio_report(sec_map:    Map[String, List[(Double, Double)]],
                            references: Map[String, (String, Double)],
                            fmt:        String) = {
    val pf_val = portfolio_value(sec_map, references)
    val pf_principal = portfolio_principal(sec_map)
    val pf_diff = pf_val - pf_principal
    val pf_percent_gain = 100 * pf_diff / pf_principal
    (pf_val, pf_percent_gain, "%")
  }

  def portfolio_value(portfolio: Map[String, List[(Double, Double)]],
                      prices:    Map[String, (String, Double)]) = {
    var pf_val = 0.0
    for (security <- portfolio.keys)
      pf_val += security_value(portfolio(security), prices(security)._2)
    pf_val
  }

  def portfolio_principal(portfolio: Map[String, List[(Double, Double)]]) = {
    var pf_principal = 0.0
    for (security <- portfolio.keys)
      pf_principal += security_principal(portfolio(security))
    pf_principal
  }

  def test(report: String) = {
    val key = """            "a"     41.60,    2.46% (20.00% of total swing)
            "b"     92.40,    1.65% (30.00% of total swing)
            "c"    204.00,    1.24% (50.00% of total swing)
-----------------------------------
      Portfolio    338.00,    1.50%"""
    report == key
  }

  def main(args: Array[String]) {
    val inputFile = if (args.length > 0) args(0) else "dummy-data.txt"
    val input = read_file_lines(inputFile)
    var data = get_body_lines(input, "tab_start:", "tab_end");
    data = data.slice(2, data.length)
    val sec_map = get_purchases(data)
    val ref_data = get_body_lines(input, "ref_start:", "ref_end")
    val references = get_references(ref_data.slice(1, ref_data.length))
    val fmt = "%15s %9.2f, %7.2f%s"
    val final_report = make_full_report(sec_map, references, fmt).mkString("\n")
    println(final_report)
    if (test(final_report))
      println("OK")
    else
      println("error")

    println()

    val dates = ref_data(1).split("\\s+").filter(d => d.length != 0)
    val pricelines = ref_data.slice(2, ref_data.length)
    val m = Map[String, List[(String, Double)]]()
    for (i <- 0 until pricelines.length) {
      val line = pricelines(i)
      val lastQuote = line.lastIndexOf('"')
      val firstField = line.substring(0, lastQuote + 1)
      val fields = line.substring(lastQuote + 1).split("\\s+").filter(d => d.length != 0)
      val tups = for (i <- 0 until dates.length) yield {
        (dates(i), fields(i).toDouble)
      }
      m += (firstField -> tups.toList)
    }

    val dateHeader = " " * (15 + 2) + dates.map(s => "%11s".format(s)).mkString("  ")
    println(dateHeader)
    println("-" * dateHeader.length)
    for ((k, l) <- m) {
      val sec_principal = security_principal(sec_map(k))
      val percs = for (price <- l) yield {
        val secVal = security_value(sec_map(k), price._2);
        100 * (secVal - sec_principal) / sec_principal
      }
      println("%15s  ".format(k) + percs.map(p => "%10.2f%s".format(p, "%")).mkString("  "))
    }
  }
}
