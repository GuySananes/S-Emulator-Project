package sserver.api;

import com.google.gson.Gson;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import present.program.FunctionSummary;
import sserver.ctx.AppContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "FunctionsServlet", urlPatterns = "/api/functions")
public class FunctionsServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            List<FunctionSummary> functionsList = AppContext.programs().listAllFunctions();

            // Convert to frontend-friendly format
            List<Map<String, Object>> functionsData = new ArrayList<>();
            for (FunctionSummary function : functionsList) {
                functionsData.add(mapFunctionToFrontendFormat(function));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("functions", functionsData);

            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("Failed to get functions: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(Map.of("error", "failed_to_get_functions")));
        }
    }

    /**
     * Maps FunctionSummary to frontend format with all required fields
     */
    private Map<String, Object> mapFunctionToFrontendFormat(FunctionSummary function) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", function.getId());
        map.put("functionName", function.getFunctionName());
        map.put("parentProgramName", function.getParentProgramName());
        map.put("owner", function.getOwner());
        map.put("instructionCount", function.getInstructionCount());
        map.put("maxDegree", function.getMaxDegree());
        return map;
    }
}