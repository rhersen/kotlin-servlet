import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "main", value = "/")
class HomeController : HttpServlet() {
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        res.writer.write("""
<!doctype html>
<html>
<head>
<meta charset=utf-8>
<title>blah</title>
</head>
<body>
<p>I'm Kotlin.</p>
</body>
</html>""")
    }
}