import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
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
            val departures = parse(getRealTrains(locationSignature))

            if (departures.isEmpty())
                writeIndex(writer)
            else
                writeStation(departures, writer)

        } catch(e: IllegalAccessException) {
            writer.write(e.message)
            res.status = 401
        }
    }

    private fun writeIndex(writer: PrintWriter) {
        writer.write("""<!doctype html>
        <html>
         <head>
          <meta content='true' name='HandheldFriendly'>
          <meta content='width=device-width, height=device-height, user-scalable=no' name='viewport'>
          <meta charset=utf-8>
          <title>kotlin servlet</title>
          <style>
          body { font-family: sans-serif; font-size: 24px }
          </style>
         </head>
         <body>
        """)
        writer.write("""
        <ol>
          <li><a href="Cst">Centralen</a>
          <li><a href="Tul">Tullinge</a>
        """)
        writer.write("""
          </ol>
         </body>
        </html>
        """)
    }

    private fun writeStation(data: List<Map<String?, String?>>, writer: PrintWriter) {
        writer.write("""<!doctype html>
        <html>
         <head>
          <meta content='true' name='HandheldFriendly'>
          <meta content='width=device-width, height=device-height, user-scalable=no' name='viewport'>
          <meta charset=utf-8>
          <title>${data.first()["LocationSignature"]}</title>
          <style>
          body { font-family: sans-serif; font-size: 24px }
          table { border-collapse: collapse; }
          th { border-right: 1px solid #999; }
          td { border: 1px solid #999; }
          </style>
         </head>
         <body>
        """)
        writer.write("""
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
}

private fun getRealTrains(locationSignature: String): InputStream {
    val url = URL("http://api.trafikinfo.trafikverket.se/v1/data.xml")
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
    return childList(node).map { it.nodeName to it.textContent }.toMap()
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