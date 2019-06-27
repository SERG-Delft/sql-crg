package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import nl.tudelft.st01.util.AggregateComponentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static nl.tudelft.st01.util.AggregateComponentFactory.COUNT_STRING;
import static nl.tudelft.st01.util.cloner.SelectCloner.copy;

/**
 * Class that generates rules for the aggregate functions such as MAX, AVG etc.
 */
public class AggregateFunctionsGenerator {

    /**
     * Main method that generates the rules for the aggregate functions.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the aggregator function
     */
    public Set<String> generate(PlainSelect plainSelect) {
        // Check if there is a Function in one of the columns. If so, generate rules for it.
        Set<String> outputAfterAggregator = new TreeSet<>();

        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getExpression() instanceof Function) {

                    Function func = (Function) selectExpressionItem.getExpression();

                    if (func.isAllColumns()) {
                        if (plainSelect.getGroupBy() != null) {
                            outputAfterAggregator.add(firstRule(plainSelect).toString());
                            outputAfterAggregator.add(secondRule(plainSelect).toString());
                        }

                        continue;
                    }

                    if (plainSelect.getGroupBy() != null) {
                        outputAfterAggregator.add(firstRule(plainSelect).toString());
                        outputAfterAggregator.add(secondRule(plainSelect).toString());
                        outputAfterAggregator.add(thirdRule(plainSelect, func).toString());
                        outputAfterAggregator.add(fourthRule(plainSelect, func).toString());

                        continue;
                    }

                    if (!COUNT_STRING.equals(func.getName().toUpperCase())) {
                        outputAfterAggregator.add(thirdRule(plainSelect, func).toString());
                        outputAfterAggregator.add(fourthRule(plainSelect, func).toString());

                        continue;
                    }

                    outputAfterAggregator.add(fourthRule(plainSelect, func).toString());
                }
            }
        }

        return outputAfterAggregator;
    }

    /**
     * Creates the aggregator statement that checks for at least one entry
     * having a certain column. Example result:<p>
     *
     * {@code SELECT COUNT(*) FROM Movies HAVING count(distinct Director) > 1}
     *
     * @param plainSelect - select to add the part to
     * @return - select item in the above specified form
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {

        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);
        plainSelectOut.setGroupByElement(null);

        SelectExpressionItem selectExpressionItem = new SelectExpressionItem(
            AggregateComponentFactory.createCountAllColumns()
        );

        List<SelectItem> selectItemList = new ArrayList<>();
        selectItemList.add(selectExpressionItem);

        plainSelectOut.setSelectItems(selectItemList);

        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);
        Function countColumn = AggregateComponentFactory.createCountColumn(groupBy, true);

        GreaterThan greaterThan = AggregateComponentFactory.createGreaterThanOne(countColumn);
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /**
     * Adds "HAVING count(*)>1" to a plainSelect item.
     * This is needed for handling aggregate operators.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect secondRule(PlainSelect plainSelect) {

        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);

        Function count = AggregateComponentFactory.createCountAllColumns();
        GreaterThan greaterThan = AggregateComponentFactory.createGreaterThanOne(count);
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /**
     * Generates the third rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the third rule for the aggregator
     */
    private PlainSelect thirdRule(PlainSelect plainSelect, Function function) {

        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);

        Function count = AggregateComponentFactory.createCountAllColumns();
        Expression expr = function.getParameters().getExpressions().get(0);

        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(count);
        leftGreaterThan.setRightExpression(AggregateComponentFactory.createCountColumn(expr, false));

        GreaterThan rightGreaterThan = AggregateComponentFactory.createGreaterThanOne(
                AggregateComponentFactory.createCountColumn(expr, true)
        );

        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);
        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }

    /**
     * Generates the fourth rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the fourth rule for the aggregator
     */
    private PlainSelect fourthRule(PlainSelect plainSelect, Function function) {

        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);

        Expression expr = function.getParameters().getExpressions().get(0);

        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(AggregateComponentFactory.createCountColumn(expr, false));
        leftGreaterThan.setRightExpression(AggregateComponentFactory.createCountColumn(expr, true));

        GreaterThan rightGreaterThan = AggregateComponentFactory.createGreaterThanOne(
            AggregateComponentFactory.createCountColumn(expr, true)
        );

        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }
}
