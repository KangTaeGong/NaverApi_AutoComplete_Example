package example.NaverApi_AutoComplete;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class SearchController {

    @Autowired MovieService movieService;

    @GetMapping(value = "/autoSearch", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public void autoSearch(Model model, HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        String id = "";
        String secret = "";
        String[] fields = {"title", "image", "pubDate", "director", "actor", "userRating"};
        String searchValue = request.getParameter("searchValue"); // 사용자가 입력한 검색어
        log.info("searchValue = {}", searchValue);

        try {
            log.info("URL Encoding 실행");
            String url = URLEncoder.encode(searchValue, "UTF-8");
            String result = movieService.search(id, secret, url);
//            log.info("MovieService.search() = {}", result);

            Map<String, Object> resultMapping = movieService.getResultMapping(result, fields);

            List<Map<String, Object>> items = (List<Map<String, Object>>) resultMapping.get("result");
            JSONArray jsonArray = new JSONArray();

            for(Map<String, Object> item : items) {
                JSONObject jsonObject = new JSONObject();
                String str = (String) item.get("title");
                str = str.replaceAll("<b>", "");
                str = str.replaceAll("</b>", "");
                log.info("str = {}", str);

                jsonObject.put("title", str); // title 값만 받아온 뒤 jsonObject로 cast
                jsonObject.put("img", item.get("image"));
                log.info("jsonObject = {}", jsonObject);

                log.info("======================================");

                jsonArray.add(jsonObject); // jsonObject값을 array에 추가
                log.info("jsonArray = {}", jsonArray);

                for(String field : fields)
                    log.info(field + "->" + item.get(field));
            }

            response.setCharacterEncoding("UTF-8");
            PrintWriter pw = response.getWriter();
            pw.print(jsonArray);
            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
