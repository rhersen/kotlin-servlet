import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.parsers.DocumentBuilderFactory

@WebServlet(name = "main", value = "/")
class HomeController : HttpServlet() {
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        if (req.requestURI.endsWith(".ico")) return

        res.contentType = "text/html";
        res.characterEncoding = "UTF-8";
        val writer = res.writer

        val servletPath = req.servletPath
        val locationSignature = servletPath.substring(servletPath.lastIndexOf('/') + 1)

        try {
            if (locationSignature.isEmpty())
                writeIndex(writer)
            else
                writeStation(parse(getRealTrains(locationSignature)), writer)
        } catch(e: IllegalAccessException) {
            writer.write(e.message)
            res.status = 401
        }
    }

    private fun writeIndex(writer: PrintWriter) {
        writer.write(header("kotlin servlet"))
        writer.write("""
          <nav class="pull-left">
            <li><a href="Spå">Spånga</a>
          </nav>
          <nav class="pull-right">
            <li><a href="Udl">Ulriksdal</a>
          </nav>
          <nav class="pull-left narrow">
            <li><a href="Sub">Sundbyberg</a>
          </nav>
          <nav class="center">
            <li><a href="Ke">Karlberg</a>
          </nav>
          <nav class="pull-right narrow">
            <li><a href="Sol">Solna</a>
          </nav>
          <nav class="center wide">
            <li><a href="Cst">Centralen</a>
            <li><a href="Sst">Södra</a>
            <li><a href="Åbe">Årstaberg</a>
          </nav>
          <nav class="pull-left narrow">
            <li><a href="Sta">Stuvsta</a>
            <li><a href="Hu">Huddinge</a>
            <li><a href="Flb">Flemingsberg</a>
            <li><a href="Tul">Tullinge</a>
            <li><a href="Tu">Tumba</a>
          </nav>
          <nav class="center">
            <li><a href="Äs">Älvsjö</a>
          </nav>
          <nav class="pull-right narrow">
            <li><a href="Fas">Farsta</a>
          </nav>
         </body>
        </html>
        """)
    }

    private fun writeStation(data: List<Map<String?, String?>>, writer: PrintWriter) {
        val locationSignature = data.first()["LocationSignature"]
        writer.write(header(locationSignature.orEmpty()))
        writer.write("""
        <h1>$locationSignature</h1>
        <table>
         <tr>
          <th>Id
          <th>To
          <th>Adv
          <th>Est
          <th>Act
        """)
        data.forEach {
            writer.write("""
            <tr>
             <td>${it["AdvertisedTrainIdent"]}
             <td>${it["ToLocation"]}
             <td>${formatTime(it["AdvertisedTimeAtLocation"])}
             <td>${formatTime(it["EstimatedTimeAtLocation"])}
             <td>${formatTime(it["TimeAtLocation"])}
        """)
        }
        writer.write("""
          </table>
         </body>
        </html>
        """)
    }

    private fun header(title: String): String {
        return """<!doctype html>
            <html>
             <head>
              <meta content='true' name='HandheldFriendly'>
              <meta content='width=device-width, height=device-height, user-scalable=no' name='viewport'>
              <meta charset=utf-8>
              <title>$title</title>
              <style>
              body { font-family: sans-serif; font-size: 24px }
              h1 { margin: 0; font-size: 32px }
              table { border-collapse: collapse; }
              th { border-right: 1px solid #999; }
              td { border: 1px solid #999; }
              li { list-style-type: none; }
              a { text-decoration: none; }
              .pull-left { float: left; width: 50%; }
              .pull-right { float: left; width: 50%; }
              .pull-right > li { text-align: right; }
              .narrow { width: 35%; }
              .center { float: left; width: 30%; }
              .center > li { text-align: center; }
              .wide { width: 100%; }
              </style>
             </head>
             <body>
            """
    }
}

private fun getRealTrains(locationSignature: String): InputStream {
    val url = URL("http://api.trafikinfo.trafikverket.se/v1.1/data.xml")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "text/xml")
    conn.doOutput = true
    val w = OutputStreamWriter(conn.outputStream)
    val dateadd = "\$dateadd"
    w.write(request(
            "AdvertisedTimeAtLocation",
            """
            <IN name='ProductInformation' value='Pendeltåg' />
            <LIKE name='AdvertisedTrainIdent' value='[0-9]$' />
            <EQ name='ActivityType' value='Avgang' />
            <EQ name='LocationSignature' value='$locationSignature' />
            <GT name='AdvertisedTimeAtLocation' value='$dateadd(-00:10:00)' />
            <LT name='AdvertisedTimeAtLocation' value='$dateadd(00:50:00)' />
            """))
    w.close()

    if (conn.responseCode != 200)
        throw RuntimeException(
                "Failed: HTTP error code: ${conn.responseCode}")

    return conn.inputStream
}

fun parse(inputStream: InputStream): List<Map<String?, String?>> {
    val input = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(inputStream))

    val response = input.firstChild
    val announcements = response.firstChild
    return childList(announcements).map { getAnnouncement(it) }
}

private fun getAnnouncement(node: Node): Map<String?, String?> {
    return childList(node).map {
        val locationName = childList(it).find { it.nodeName == "LocationName" }
        it.nodeName to (locationName ?: it).textContent
    }.toMap()
}

private fun childList(n: Node): List<Node> {
    val childNodes = n.childNodes
    return (0..childNodes.length - 1).map { childNodes.item(it) }
}

fun request(orderBy: String, filters: String): String {
    return """
    <REQUEST>
     <LOGIN authenticationkey='${getKey()}' />
     <QUERY objecttype='TrainAnnouncement' orderby='$orderBy'>
      <FILTER>
       <AND>$filters</AND>
      </FILTER>
      <INCLUDE>LocationSignature</INCLUDE>
      <INCLUDE>AdvertisedTrainIdent</INCLUDE>
      <INCLUDE>AdvertisedTimeAtLocation</INCLUDE>
      <INCLUDE>EstimatedTimeAtLocation</INCLUDE>
      <INCLUDE>TimeAtLocation</INCLUDE>
      <INCLUDE>ProductInformation</INCLUDE>
      <INCLUDE>ToLocation</INCLUDE>
      <INCLUDE>ActivityType</INCLUDE>
     </QUERY>
    </REQUEST>"""
}