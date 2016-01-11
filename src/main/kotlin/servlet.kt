import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.InputStream
import java.io.OutputStreamWriter
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
        res.contentType = "text/html";
        res.characterEncoding = "UTF-8";
        val writer = res.writer
        try {
            val responseData = parse(getRealTrains())

            writer.write("""<!doctype html>
            <html>
             <head>
              <meta charset=utf-8>
              <title>${responseData.first()["LocationSignature"]}</title>
             </head>
             <body>
            """)
            writer.write("""
            <table>
             <tr>
              <th>Train
              <th>To
              <th>Advertised
              <th>Estimated
              <th>Actual
            """)
            responseData.forEach {
                writer.write("""
                <tr>
                 <td>${it["AdvertisedTrainIdent"]}
                 <td>${it["ToLocation"]}
                 <td>${it["AdvertisedTimeAtLocation"]}
                 <td>${it["EstimatedTimeAtLocation"]}
                 <td>${it["TimeAtLocation"]}
            """)
            }
            writer.write("""
             </body>
            </html>
            """)
        } catch(e: IllegalAccessException) {
            writer.write(e.message)
            res.status = 401
        }
    }
}

private fun getRealTrains(): InputStream {
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
            <EQ name='LocationSignature' value='Tul' />
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