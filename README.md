## AutoComplete

해석하는 그래도 자동 완성 기능이다.<br>
사용자가 검색창에 입력하는 값을 실시간으로 확인 하면서 사용자가 원하는 데이터를 찾아서 제공.

<br>

## Project

현재 코드는 네이버 영화 API와 연동하였다.<br>
사용자가 실시간으로 입력하는 값을 API를 통해 검색 후, 일치하는 영화 제목을 제공해 주면서 자동 완성 기능을 제공해준다.<br>
자동 완성 리스트에는 영화의 제목과 포스터 사진이 들어간다.<br>

네이버 API를 통해 가져오는 값은 Json 형식이기 때문에 Json 데이터를 처리할 수 있는 Json-Simple 라이브러리 사용.<br>

Front단은 AutoComplete 기능을 위해 Jquery의 `ajax()` 메서드 사용

<br>

>**Json-Simple**<br>
Json 데이터를 처리하기 위한 자바 라이브러리.


>**Ajax - Asynchronous Javascript And XML**<br>
JavaScript를 이용해 **서버와 브라우저가 비동기 방식으로 데이터를 교환할 수 있는 통신 기능**

<br>

>**※ 네이버 영화 서비스 페이지 운영 종료로 인해 오픈 API 서비스도 중단 되면서 현재 사용 불가.**

<br>

## Code

**사전에 `build.gradle`에 Json-simple 관련 코드 추가**
```gradle
dependencies {
	implementation group: 'com.googlecode.json-simple', name: 'json-simple', version: '1.1.1'
}
```

<br>

### 주요 코드

```java
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
```

- 네이버 API를 통해 지급받은 id, secret 사용.
- 전체 데이터 중에서 필요한 데이터만 추출하기 위해 `fileds[]`에 Json 데이터의 key값 입력.
- 클라이언트가 실시간으로 입력하는 값을 `request.getParameter()`를 통해 받아온다.
  - 실시간으로 입력하는 값을 `URLEncoder`로 인코딩하여 MovieService에 값을 넘겨준다.
  - 그에 따라 반환되어 오는 결과값을 `fields[]`와 함께 다시 MovieService에 넘겨주면서 전체 데이터에서 필요한 데이터만 추출.
- 최종 결과 값을 `JsonSimple`을 이용해 Json 형태로 매핑한 뒤 `PrintWriter`를 통해 출력.(Front단에 값을 넘겨준다.)

<br>

#### Front

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <title>Document</title>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

    <style>
        #autocomplete-image {
            width: 100px;
            height: 120px;
            vertical-align: middle;
        }

        #autocomplete-title {
            padding-left: 10px;
        }

        .ui-autocomplete-loading {
            background:url('img/loading.gif') no-repeat right center
        }
    </style>
</head>
<body>
    <div class="search-box">
        <input type="text" placeholder="영화를 입력 후 Enter" id="autocompleteText" aria-label="Search">
            <i class="fas fa-search"></i>
    </div>

    <script>
        $(document).ready(function() {
            $( "#autocompleteText" ).autocomplete({
                source : function( request, response ) {
                    $.ajax({
                        type : 'get',
                        url: "autoSearch",
                        dataType: "json",
                        data: {
                          searchValue: request.term // 사용자가 입력하는 값
                        },
                        success: function( data ) {
                            response(
                                $.map( data, function( item ) {
                                    return {
                                        label: item.title, // 목록에 표시되는 값
                                        value: item.title, // 선택 시 input창에 표시되는 값
                                        avatar: item.img
                                    }
                                })
                            );
                        }
                    });
                },
                focus : function(event, ui) { // 포커스 시 이벤트
                    return false; // 방향키로 바로 선택 방지(enter시 선택)
                },
                minLength: 1,
                delay : 1000, // 입력창에 글자가 써지고나서 autocomplete 이벤트 발생될 때 까지 지연 시간(ms)
                close : function(event) { // 자동완성 창 닫아질 때의 이벤트
                    console.log(event);
                }
            }).autocomplete('instance')._renderItem = function(ul, item) { // UI 변경 부
                return $('<li>') //기본 tag가 li
                .append('<div>' +
                            '<img id="autocomplete-image" src="' + item.avatar + '"/>' +
                             '<span id="autocomplete-title">' + item.label + '</span>' + '</div>') // 원하는 모양의 HTML 만들면 됨
                .appendTo(ul);
            };
        });
    </script>
</body>
</html>
```
- 자동 완성 리스트에 영화 포스터 이미지도 추가하고 싶기 때문에, UI 변경부에서 원하는대로 수정하였다.
