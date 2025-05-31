package com.jana.service;

import com.jana.model.CoinModel;
import com.jana.response.ApiResponse;
import com.jana.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChatbotServiceImpl implements ChatbotService{

    private double convertToDouble(Object value){
        if (value instanceof Integer){
            return ((Integer)value).doubleValue();
        }
        else if(value instanceof Long){
            return ((Long)value).doubleValue();
        } else if (value instanceof Double) {
            return (Double)value;
        }
        else throw new IllegalArgumentException("unsupported type" + value.getClass().getName());
    }

    public CoinModel makeApiRequest(String currencyName) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/bitcoin";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
        Map <String ,Object> responseBody = responseEntity.getBody();

        if(responseBody != null){
            Map<String, Object> image = (Map<String,Object>) responseBody.get("image");
            Map<String, Object> marketData = (Map<String,Object>) responseBody.get("market_data");

            CoinModel coinModel = new CoinModel();
            coinModel.setId((String) responseBody.get("id"));
            coinModel.setName((String) responseBody.get("name"));
            coinModel.setSymbol((String) responseBody.get("symbol"));
            coinModel.setImage((String) image.get("large"));


            //market Data
            coinModel.setCurrentPrice(convertToDouble(((Map<String,Object>)marketData.get("current_price")).get("usd")));
            coinModel.setMarketCap(convertToDouble(((Map<String,Object>)marketData.get("market_cap")).get("usd")));
            coinModel.setMarketCapRank(convertToDouble((marketData.get("market_cap_rank"))));
            coinModel.setTotalVolume(convertToDouble(((Map<String,Object>)marketData.get("total_volume")).get("usd")));
            coinModel.setHigh24h(convertToDouble(((Map<String,Object>)marketData.get("high_24h")).get("usd")));
            coinModel.setLow24h(convertToDouble(((Map<String,Object>)marketData.get("low_24h")).get("usd")));
            coinModel.setPriceChange24h(convertToDouble((marketData.get("price_change_24h"))));
            coinModel.setPriceChangePercentage24h(convertToDouble((marketData.get("price_change_percentage_24h"))));
            coinModel.setMarketCapChange24h(convertToDouble((marketData.get("market_cap_change_24h"))));
            coinModel.setMarketCapChangePercentage24h(convertToDouble((marketData.get("market_cap_change_percentage_24h"))));
            coinModel.setCirculatingSupply(convertToDouble((marketData.get("circulating_supply"))));
            coinModel.setTotalSupply(convertToDouble((marketData.get("total_supply"))));


        return coinModel;
        }
        throw new Exception("coin not found");
    }


public FunctionResponse getFunctionResponse(String prompt) {
    String GEMINI_API_KEY = "AIzaSyCzkgBHXkM32X1sDQUBIrliyikH9sOIxKY";
    String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    // Build JSON body
    JSONObject requestBodyJson = new JSONObject()
            .put("contents", new JSONArray()
                    .put(new JSONObject()
                            .put("parts", new JSONArray()
                                    .put(new JSONObject()
                                            .put("text", prompt)
                                    )
                            )
                    )
            )
            .put("tools", new JSONArray()
                    .put(new JSONObject()
                            .put("functionDeclarations", new JSONArray()
                                    .put(new JSONObject()
                                            .put("name", "getCoinDetails")
                                            .put("description", "Get the coin details from given currency object")
                                            .put("parameters", new JSONObject()
                                                    .put("type", "OBJECT")
                                                    .put("properties", new JSONObject()
                                                            .put("currencyName", new JSONObject()
                                                                    .put("type", "STRING")
                                                                    .put("description", "The currency name, id, symbol.")
                                                            )
                                                            .put("currencyData", new JSONObject()
                                                                    .put("type", "STRING")
                                                                    .put("description", "Currency Data including price, name, market_cap, etc.")
                                                            )
                                                    )
                                                    .put("required", new JSONArray()
                                                            .put("currencyName")
                                                            .put("currencyData")
                                                    )
                                            )
                                    )
                            )
                    )
            );

    // Prepare HTTP request
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson.toString(), headers);

    // Send request using RestTemplate
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

    // Read and print response
    String responseBody = response.getBody();
    JSONObject jsonObject = new JSONObject(responseBody);

    JSONArray candidates = jsonObject.getJSONArray("candidates");
    JSONObject firstCandidate = candidates.getJSONObject(0);

    JSONObject content = firstCandidate.getJSONObject("content");
    JSONArray parts = content.getJSONArray("parts");
    JSONObject firstPart = parts.getJSONObject(0);
    if (!firstPart.has("functionCall")) {
        System.out.println("⚠️ No function call returned by Gemini.");
        System.out.println("🔎 Full response: " + responseBody);
        return null;
    }
    JSONObject functionCall  = firstPart.getJSONObject("functionCall");

    String functionName  = functionCall.getString("name");
    JSONObject args  = functionCall.getJSONObject("args");
    String currencyName = args.getString("currencyName");
    String currencyData = args.getString("currencyData");

    System.out.println("FunctionName: " +functionName);
    System.out.println("CurrencyName: "+currencyName);
    System.out.println("CurrencyData: "+currencyData );


    FunctionResponse res = new FunctionResponse();
    res.setFunctionName(functionName);
    res.setCurrencyName(currencyName);
    res.setCurrencyData(currencyData);

    return res; // Replace with actual return when FunctionResponse is defined
}

    @Override
    public ApiResponse getCoinDetails(String prompt) throws Exception {

        String GEMINI_API_KEY = "AIzaSyCzkgBHXkM32X1sDQUBIrliyikH9sOIxKY";
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
        CoinModel apiResponse = makeApiRequest(prompt);
        FunctionResponse res= getFunctionResponse(prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);




        String body="{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"model\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"functionCall\": {\n" +
                "            \"name\": \"getCoinDetails\",\n" +
                "            \"args\": {\n" +
                "              \"currencyName\": \"" +res.getCurrencyName() +"\",\n" +
                "              \"currencyData\": \""+ res.getCurrencyData() + "\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"function\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"functionResponse\": {\n" +
                "            \"name\": \"getCoinDetails\",\n" +
                "            \"response\": {\n" +
                "              \"name\": \"getCoinDetails\",\n" +
                "              \"content\": " + apiResponse + "\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get crypto currency data from given currency object.\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency Name, id, symbol .\"\n" +
                "              },\n" +
                "              \"currencyData\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency data id, symbol, current price, image, market cap extra... \"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\",\"currencyData\"]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"find_theaters\",\n" +
                "          \"description\": \"find theaters based on location and optionally movie title which is currently playing in theaters\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"location\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The city and state, e.g. San Francisco, CA or a zip code e.g. 95616\"\n" +
                "              },\n" +
                "              \"movie\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Any movie title\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"location\"]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"get_showtimes\",\n" +
                "          \"description\": \"Find the start times for movies playing in a specific theater\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"location\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The city and state, e.g. San Francisco, CA or a zip code e.g. 95616\"\n" +
                "              },\n" +
                "              \"movie\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Any movie title\"\n" +
                "              },\n" +
                "              \"theater\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Name of the theater\"\n" +
                "              },\n" +
                "              \"date\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Date for requested showtime\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"location\", \"movie\", \"theater\", \"date\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";



        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);

        System.out.println("Response: " + response.getBody());
//        ReaderContext ctx = JsonPath.parse(response.getBody());
//
//        String text = ctx.read("$.candidates[0].content.parts[0].text");
        ApiResponse ans=new ApiResponse();
//        ans.setMessage(text);

        return ans;

    }

    @Override
    public String simpleChat(String prompt) {
        String GEMINI_API_KEY ="AIzaSyCzkgBHXkM32X1sDQUBIrliyikH9sOIxKY";
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="+GEMINI_API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = new JSONObject()
                .put("contents", new JSONArray()
                .put(new JSONObject()
                        .put("parts", new JSONArray()
                                .put(new JSONObject().put("text",prompt))
                        ))).toString();

        HttpEntity<String> requestEntity =  new HttpEntity<>(requestBody,headers);

        RestTemplate restTemplate =new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL,requestEntity, String.class);


        return response.getBody();
    }
}
