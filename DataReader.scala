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
    val match0 = """\s*("[^"]+")\s+([\.0-9]+)""".r
    line match {
      case match0(name, price) => (name, price.toDouble)
      case _ => ("foo", 0.0)
    }
  }

  def make_full_report(sec_map:    Map[String, List[(Double, Double)]],
                       references: Map[String, (String, Double)],
                       fmt:        String) = {
    val fullReport = for (key <- sec_map.keys) yield {
      val sec_report = make_security_report(key, sec_map, references)
      fmt.format(key, sec_report._1, sec_report._2, sec_report._3)
    }
    fullReport
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

  def main(args: Array[String]) {
    val input = read_file_lines("dummy-data.txt")
    var data = get_body_lines(input, "tab_start:", "tab_end");
    data = data.slice(2, data.length)
    val sec_map = get_purchases(data)
    val ref_data = get_body_lines(input, "ref_start:", "ref_end")
    val references = get_references(ref_data.slice(1, ref_data.length))
    val fmt = "%15s %9.2f, %7.2f%s"
    val final_report = make_full_report(sec_map, references, fmt)
    println(final_report.mkString("\n"))
  }
}
