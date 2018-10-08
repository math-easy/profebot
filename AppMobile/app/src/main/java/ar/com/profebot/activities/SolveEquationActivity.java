package ar.com.profebot.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.profebot.activities.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.com.profebot.Models.MultipleChoiceStep;
import ar.com.profebot.resolutor.service.ResolutorService;
import ar.com.profebot.service.EquationManager;
import ar.com.profebot.service.ExpressionsManager;
import ar.com.profebot.service.JustificationsService;
import ar.com.profebot.service.RVMultipleChoiceAdapter;

public class SolveEquationActivity extends GlobalActivity {

    public static final String CONTEXT_OF_RESOLUTION_IS_EQUATION_WITH_FINITE_SOLUTIONS = "equation_finite_solutions";
    public static final String CONTEXT_OF_RESOLUTION_IS_EQUATION_WITH_INFINITE_SOLUTIONS = "equation_infinite_solutions";
    public static final String CONTEXT_OF_RESOLUTION_IS_EQUATION_WITHOUT_SOLUTIONS = "equation_without_solutions";

    public static final String CONTEXT_OF_RESOLUTION_IS_INEQUATION_WITH_INTERVAL_SOLUTIONS = "inequation_interval_solutions";
    public static final String CONTEXT_OF_RESOLUTION_IS_INEQUATION_WITHOUT_SOLUTIONS = "inequation_without_solutions";

    public static final String CONTEXT_OF_RESOLUTION_IS_DOMAIN = "domain";
    public static final String CONTEXT_OF_RESOLUTION_IS_IMAGE = "image";
    public static final String CONTEXT_OF_RESOLUTION_IS_ROOTS = "roots_of_function";
    public static final String CONTEXT_OF_RESOLUTION_IS_ORIGIN_ORD = "origin_ord_of_function";

    public static final int EQUATION_OR_INEQUATION = 0;
    public static final int FUNCTION = 1;

    private static List<MultipleChoiceStep> multipleChoiceSteps;
    private static RVMultipleChoiceAdapter adapter;
    public static RecyclerView recyclerView;
    public static SolveEquationActivity context;
    private static Button seeSummary;
    private static String contextOfResolution;
    private static Integer typeOfContextOfResolution;
    private static String title;
    private static String lastEquation;
    private static Map<String, String> contextOfResolutionTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.solve_equation_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar_id);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);

        context = this;
        
        recyclerView = findViewById(R.id.rv_resolution_id);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        multipleChoiceSteps = this.initializeMultipleChoiceSteps();

        if(!multipleChoiceSteps.isEmpty()){
            adapter = new RVMultipleChoiceAdapter(multipleChoiceSteps.get(0), multipleChoiceSteps, new EquationManager());
            lastEquation = multipleChoiceSteps.get(multipleChoiceSteps.size() - 1).getNewEquationBase();
        }else{
            adapter = new RVMultipleChoiceAdapter(null, new ArrayList<>(), new EquationManager());
            lastEquation = ExpressionsManager.getTreeOfExpression().toExpression();
        }
        recyclerView.setAdapter(adapter);

        EquationManager.setContext(this);
        LinearLayout equationSummarySection = findViewById(R.id.final_summary_section_id);
        LinearLayout recycleViewSection = findViewById(R.id.recycle_view_section_id);
        setContextOfResolution();
        title = "Ecuación terminada";

        if(multipleChoiceSteps.isEmpty()){
            equationSummarySection.findViewById(R.id.return_to_resolution_id).setVisibility(View.GONE);
            adapter.getManager().showFinalSummary(recycleViewSection, equationSummarySection,
                    contextOfResolutionTexts.get("first"), ExpressionsManager.getTreeOfExpression().toExpression(),
                    contextOfResolutionTexts.get("second"), lastEquation, title, contextOfResolutionTexts.get("type"));
        }

        seeSummary = findViewById(R.id.see_summary_id);
        final String lastEquationToButton = lastEquation;
        seeSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getManager().showFinalSummary(recycleViewSection, equationSummarySection,
                        contextOfResolutionTexts.get("first"), ExpressionsManager.getTreeOfExpression().toExpression(),
                        contextOfResolutionTexts.get("second"), lastEquationToButton, title, contextOfResolutionTexts.get("type"));
            }
        });

        disableSummary();
    }

    public static void setTypeOfContextOfResolution(Integer typeOfContextOfResolution) {
        SolveEquationActivity.typeOfContextOfResolution = typeOfContextOfResolution;
    }

    private void setContextOfResolution() {
        if(typeOfContextOfResolution == EQUATION_OR_INEQUATION){
            // Equation
            if(lastEquation.contains("=")){
                String[] members = lastEquation.split("=");
                if(members[0].equals(members[1])){
                    contextOfResolution = CONTEXT_OF_RESOLUTION_IS_EQUATION_WITH_INFINITE_SOLUTIONS;
                }else if(members[0].toUpperCase().equals("X")){
                    contextOfResolution = CONTEXT_OF_RESOLUTION_IS_EQUATION_WITH_FINITE_SOLUTIONS;
                }else{
                    try{
                        Double.parseDouble(members[0]);
                        Double.parseDouble(members[1]);
                        contextOfResolution = CONTEXT_OF_RESOLUTION_IS_EQUATION_WITHOUT_SOLUTIONS;
                    }catch (Exception e){
                        contextOfResolution = "";
                    }
                }

            }else{ // Inequation
                // TODO: ver en qué formato llega
                contextOfResolution = "";
            }
        }

        contextOfResolutionTexts = JustificationsService.getContextOfResolutionTexts(contextOfResolution, this);
        String[] members;
        switch (contextOfResolution){
            case CONTEXT_OF_RESOLUTION_IS_EQUATION_WITH_FINITE_SOLUTIONS:
                contextOfResolutionTexts = EquationManager.fixResolutionTextsForRoots(contextOfResolutionTexts, lastEquation);
                break;
            case CONTEXT_OF_RESOLUTION_IS_DOMAIN:
                contextOfResolutionTexts = EquationManager.fixResolutionTextsForFunctionIntervals(contextOfResolutionTexts, lastEquation);
                break;
            case CONTEXT_OF_RESOLUTION_IS_IMAGE:
                contextOfResolutionTexts = EquationManager.fixResolutionTextsForFunctionIntervals(contextOfResolutionTexts, lastEquation);
                break;
            case CONTEXT_OF_RESOLUTION_IS_ROOTS:
                contextOfResolutionTexts = EquationManager.fixResolutionTextsForRoots(contextOfResolutionTexts, lastEquation);
                break;
            case CONTEXT_OF_RESOLUTION_IS_ORIGIN_ORD:
                members = lastEquation.split("=");
                contextOfResolutionTexts = JustificationsService.replacePatterns(contextOfResolutionTexts, "second", "/valor/", members[1]);
                break;
            case CONTEXT_OF_RESOLUTION_IS_INEQUATION_WITH_INTERVAL_SOLUTIONS:
                // TODO: falta ver como me llega la info del back
                break;
        }
    }

    private void disableSummary(){
        seeSummary.setBackgroundResource(R.drawable.rounded_corners_disable_button);
        seeSummary.setTextColor(Color.GRAY);
        seeSummary.setEnabled(false);
    }

    public void enableSummary(){
        seeSummary.setBackgroundResource(R.drawable.rounded_corners_polynomial_summary);
        seeSummary.setTextColor(Color.WHITE);
        seeSummary.setEnabled(true);

        LinearLayout finalSummarySection = findViewById(R.id.final_summary_section_id);
        LinearLayout recycleViewSection = findViewById(R.id.recycle_view_section_id);
        seeSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getManager().showFinalSummary(recycleViewSection, finalSummarySection,
                        contextOfResolutionTexts.get("first"), ExpressionsManager.getTreeOfExpression().toExpression(),
                        contextOfResolutionTexts.get("second"), lastEquation, title, contextOfResolutionTexts.get("type"));
            }
        });
    }

    public static SolveEquationActivity getContext(){
        return context;
    }

    private List<MultipleChoiceStep> initializeMultipleChoiceSteps(){
        List<MultipleChoiceStep> steps;
        steps = (new ResolutorService()).resolveExpression(ExpressionsManager.getTreeOfExpression(), context);
        return steps;
    }

    @Override
    public boolean onSupportNavigateUp() {
        returnToEnterNewEquation();
        return true;
    }

    @Override
    public void onBackPressed() {
        returnToEnterNewEquation();
    }

    private void returnToEnterNewEquation(){
        ExpressionsManager.setEquationDrawn(null);
        startActivity(new Intent(this, EnterEquationOptionsActivity.class));
    }
}
