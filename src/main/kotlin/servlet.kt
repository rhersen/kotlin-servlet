import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.parsers.DocumentBuilderFactory

@WebServlet(name = "main", value = "/")
class HomeController : HttpServlet() {
    private var cache: ArrayDeque<String>? = null

    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        if (req.requestURI.endsWith(".ico")) return
        val departuresPath = Regex(""".*api/departures/(.+)$""").matchEntire(req.servletPath)

        if (departuresPath != null) {
            val location = departuresPath.groups[1]

            if (location != null) {
                res.contentType = "application/json";
                res.characterEncoding = "UTF-8";
                writeDepartures(getRealTrains(location.value, "json"), res.writer)
            }

            return
        }

        val stationsPath = Regex(""".*api/stations.*""").matchEntire(req.servletPath)
        if (stationsPath != null) {
            res.contentType = "application/json";
            res.characterEncoding = "UTF-8";

            if (cache == null) {
                cache = ArrayDeque()
                val reader = BufferedReader(InputStreamReader(getStations()))

                var l = reader.readLine()
                while (l != null) {
                    cache?.addLast(l)
                    l = reader.readLine()
                }
            }

            val writer = res.writer
            for (line in cache.orEmpty())
                writer.write(line)

            return
        }

        val currentPath = Regex(""".*api/current.*""").matchEntire(req.servletPath)
        if (currentPath != null) {
            res.contentType = "application/json";
            res.characterEncoding = "UTF-8";

            writeDepartures(getCurrentTrains(), res.writer)

            return
        }

        res.contentType = "text/html";
        res.characterEncoding = "UTF-8";
        val writer = res.writer

        val servletPath = req.servletPath
        val locationSignature = servletPath.substring(servletPath.lastIndexOf('/') + 1)

        try {
            if (locationSignature.isEmpty())
                writeIndex(writer)
            else
                writeStation(parse(getRealTrains(locationSignature, "xml")), writer)
        } catch(e: IllegalAccessException) {
            writer.write(e.message)
            res.status = 401
        }
    }

    private fun writeDepartures(realTrains: InputStream, writer: PrintWriter) {
        val reader = BufferedReader(InputStreamReader(realTrains))

        var readLine = reader.readLine()
        while (readLine != null) {
            writer.write(readLine)
            readLine = reader.readLine()
        }
    }

    private fun writeIndex(writer: PrintWriter) {
        writer.write(header("kotlin servlet"))
        writer.write("""
          <nav class="pull-left">
            <div><a href="Spå">Spånga</a></div>
          </nav>
          <nav class="pull-right">
            <div><a href="Udl">Ulriksdal</a></div>
          </nav>
          <nav class="pull-left narrow">
            <div><a href="Sub">Sundbyberg</a></div>
          </nav>
          <nav class="center">
            <div><a href="Ke">Karlberg</a></div>
          </nav>
          <nav class="pull-right narrow">
            <div><a href="Sol">Solna</a></div>
          </nav>
          <nav class="center wide">
            <div><a href="Cst">Centralen</a></div>
            <div><a href="Sst">Södra</a></div>
            <div><a href="Åbe">Årstaberg</a></div>
          </nav>
          <nav class="pull-left narrow">
            <div><a href="Sta">Stuvsta</a></div>
            <div><a href="Hu">Huddinge</a></div>
            <div><a href="Flb">Flemingsberg</a></div>
            <div><a href="Tul">Tullinge</a></div>
            <div><a href="Tu">Tumba</a></div>
          </nav>
          <nav class="center">
            <div><a href="Äs">Älvsjö</a></div>
          </nav>
          <nav class="pull-right narrow">
            <div><a href="Fas">Farsta</a></div>
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
              a { text-decoration: none; }
              .pull-left { float: left; width: 50%; }
              .pull-right { float: left; width: 50%; }
              .pull-right > div { text-align: right; }
              .narrow { width: 35%; }
              .center { float: left; width: 30%; }
              .center > div { text-align: center; }
              .wide { width: 100%; }
              </style>
             </head>
             <body>
            """
    }
}

private fun getRealTrains(locationSignature: String, format: String): InputStream {
    val url = URL("http://api.trafikinfo.trafikverket.se/v1.1/data.$format")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "text/xml")
    conn.doOutput = true
    val w = OutputStreamWriter(conn.outputStream)
    val dateadd = "\$dateadd"
    w.write("""
    <REQUEST>
     <LOGIN authenticationkey='${getKey()}' />
     <QUERY objecttype='TrainAnnouncement' orderby='${"AdvertisedTimeAtLocation"}'>
      <FILTER>
       <AND>
        <IN name='ProductInformation' value='Pendeltåg' />
        <EQ name='ActivityType' value='Avgang' />
        <EQ name='LocationSignature' value='$locationSignature' />
        <OR>
         <AND>
          <GT name='AdvertisedTimeAtLocation' value='$dateadd(-00:15:00)' />
          <LT name='AdvertisedTimeAtLocation' value='$dateadd(00:59:00)' />
         </AND>
         <GT name='EstimatedTimeAtLocation' value='$dateadd(-00:15:00)' />
        </OR>
       </AND>
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
    )
    w.close()

    if (conn.responseCode != 200)
        throw RuntimeException(
                "Failed: HTTP error code: ${conn.responseCode}")

    return conn.inputStream
}

private fun getCurrentTrains(): InputStream {
    val url = URL("http://api.trafikinfo.trafikverket.se/v1.1/data.json")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "text/xml")
    conn.doOutput = true
    val w = OutputStreamWriter(conn.outputStream)
    val dateadd = "\$dateadd"
    w.write("""
    <REQUEST>
     <LOGIN authenticationkey='${getKey()}' />
     <QUERY objecttype='TrainAnnouncement' orderby='${"AdvertisedTrainIdent"}'>
      <FILTER>
       <AND>
        <IN name='ProductInformation' value='Pendeltåg' />
        <GT name='TimeAtLocation' value='$dateadd(-00:04:00)' />
        <LT name='TimeAtLocation' value='$dateadd(00:04:00)' />
       </AND>
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
    )
    w.close()

    if (conn.responseCode != 200)
        throw RuntimeException(
                "Failed: HTTP error code: ${conn.responseCode}")

    return conn.inputStream
}

private fun getStations(): InputStream {
    val url = URL("http://api.trafikinfo.trafikverket.se/v1.1/data.json")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "text/xml")
    conn.doOutput = true
    val w = OutputStreamWriter(conn.outputStream)
    w.write("""
    <REQUEST>
     <LOGIN authenticationkey='${getKey()}' />
     <QUERY objecttype='TrainStation'>
      <FILTER>
       <OR>
         <IN name='CountyNo' value='1' />
         <EQ name='LocationSignature' value='U' />
         <EQ name='LocationSignature' value='Kn' />
         <EQ name='LocationSignature' value='Gn' />
         <EQ name='LocationSignature' value='Bål' />
       </OR>
      </FILTER>
      <INCLUDE>LocationSignature</INCLUDE>
      <INCLUDE>AdvertisedShortLocationName</INCLUDE>
     </QUERY>
    </REQUEST>"""
    )
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
