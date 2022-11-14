package example.NaverApi_AutoComplete;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

//    @RequestMapping(value = "/autoSearch", method = RequestMethod.GET,
//            produces = "application/json; charset=UTF-8")
    @GetMapping("/autoSearch")
    @ResponseBody
    public void autoSearch(Model model, HttpServletRequest request,
                           HttpServletResponse response, String autocompleteText) throws IOException {

        String id = "";
        String secret = "";
        String[] fields = {"title", "image", "pubDate", "director", "actor", "userRating"};
        String searchValue = request.getParameter("searchValue"); // 사용자가 입력한 검색어
        log.info("searchValue = {}", searchValue);
        log.info("autocompleteText = {}", autocompleteText);
        try {
            String url = URLEncoder.encode(searchValue, "UTF-8");
            String result = movieService.search(id, secret, url);

            Map<String, Object> resultMapping = movieService.getResultMapping(result, fields);
//            JSONArray resultMapping = movieService.getResultMapping(result, fields);

            List<Map<String, Object>> items = (List<Map<String, Object>>) resultMapping.get("result");

            JSONArray jsonArray = new JSONArray();
            for(Map<String, Object> item : items) {
                JSONObject jsonObject = (JSONObject) item.get("title");

                jsonArray.add(jsonObject);
                log.info("======================================");

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
