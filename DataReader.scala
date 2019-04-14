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
    val references = Map[String, List[(String, Double)]]()
    val dates = input(0).split("\\s+").filter(d => d.length != 0)
    val refData = input.tail
    for (i <- 0 until refData.length) {
      val line = refData(i)
      val lastQuote = line.lastIndexOf('"')
      val firstField = line.substring(0, lastQuote + 1)
      val fields = line.substring(lastQuote + 1).split("\\s+").filter(d => d.length != 0)
      val tups = for (i <- 0 until dates.length) yield {
        (dates(i), fields(i).toDouble)
      }
      references += (firstField -> tups.toList)
    }
    references
  }

  def getLatestReferences(references: Map[String, List[(String, Double)]]) = {
    val latest = Map[String, (String, Double)]()
    for ((k,l) <- references) {
      val sortedList = l.sortWith(_._1 > _._1)
      latest += (k -> sortedList(0))
    }
    latest
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
      val currSecMap = sec_map(key)
      val currReference = references(key)
      val secReport = make_security_report(currSecMap, currReference)
      val diff = security_value(currSecMap, currReference._2) - security_principal(currSecMap)
      fullReport += fmt.format(key, secReport._1, secReport._2, "%")
      diffs += diff
    }

    val abs_diffs = diffs.map(x => Math.abs(x))
    val abs_pf_diff = abs_diffs.reduce((x,y) => x + y)
    for (i <- 0 until abs_diffs.length) {
      val frac = 100 * abs_diffs(i) / abs_pf_diff
      fullReport(i) = fullReport(i) + " (%5.2f%s of total swing)".format(frac, "%")
    }

    val pfReport = make_portfolio_report(sec_map, references)
    fullReport.sortWith(_ < _) :+ "-----------------------------------" :+
      fmt.format("Portfolio", pfReport._1, pfReport._2, "%")
  }

  def make_security_report(sec_map:    List[(Double, Double)],
                           references: (String, Double)) = {
    val sec_price = references._2
    val sec_principal = security_principal(sec_map)
    val sec_val = security_value(sec_map, sec_price)
    val diff = sec_val - sec_principal
    val percent_gain = 100 * diff / sec_principal
    (sec_val, percent_gain)
  }

  def security_principal(purchases: List[(Double, Double)]) = {
    purchases.map { case (a, b) => a * b }.sum
  }

  def security_value(purchases: List[(Double, Double)], price: Double) = {
    price * purchases.map { case (a, b) => b }.sum
  }

  def make_portfolio_report(sec_map:    Map[String, List[(Double, Double)]],
                            references: Map[String, (String, Double)]) = {
    val pf_val = portfolio_value(sec_map, references)
    val pf_principal = portfolio_principal(sec_map)
    val pf_diff = pf_val - pf_principal
    val pf_percent_gain = 100 * pf_diff / pf_principal
    (pf_val, pf_percent_gain)
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

  def makeDateHeader(refData: Array[String]) = {
    val dates = refData(0).split("\\s+").filter(d => d.length != 0)
    " " * (15 + 2) + dates.map(s => "%11s".format(s)).mkString("  ")
  }

  def makeAnotherReport(sec_map:    Map[String, List[(Double, Double)]],
                        references: Map[String, List[(String, Double)]]) = {
    for ((k, l) <- references) yield {
      val sec_principal = security_principal(sec_map(k))
      val percs = for (price <- l) yield {
        val secVal = security_value(sec_map(k), price._2);
        100 * (secVal - sec_principal) / sec_principal
      }
      "%15s  ".format(k) + percs.map(p => "%10.2f%s".format(p, "%")).mkString("  ")
    }
  }

  def test(report: String) = {
    val key = """            "a"     42.00,    3.45% (20.00% of total swing)
            "b"     93.00,    2.31% (30.00% of total swing)
            "c"    205.00,    1.74% (50.00% of total swing)
-----------------------------------
      Portfolio    340.00,    2.10%"""
    report == key
  }

  def main(args: Array[String]) {
    val doTest = args.length < 1
    val inputFile = if (!doTest) args(0) else "dummy-data.txt"
    val input = read_file_lines(inputFile)

    var data = get_body_lines(input, "tab_start:", "tab_end");
    val sec_map = get_purchases(data.slice(2, data.length))

    val ref_data = get_body_lines(input, "ref_start:", "ref_end")
    val references = get_references(ref_data.slice(1, ref_data.length))
    val latestRefs = getLatestReferences(references)

    val fmt = "%15s %9.2f, %7.2f%s"
    val final_report = make_full_report(sec_map, latestRefs, fmt).mkString("\n")
    println(final_report)
    if (doTest) {
      if (test(final_report))
        println("OK")
      else
        println("error")
    }

    println()

    val dateHeader = makeDateHeader(ref_data.tail)
    val foobar = makeAnotherReport(sec_map, references)
    val foolist = dateHeader :: "-" * dateHeader.length :: foobar.toList.sortWith(_ < _)
    println(foolist.mkString("\n"))
  }
}
