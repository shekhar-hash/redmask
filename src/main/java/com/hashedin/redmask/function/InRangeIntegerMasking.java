package com.hashedin.redmask.function;

import com.hashedin.redmask.common.MaskingQueryUtil;
import com.hashedin.redmask.common.MaskingRuleDef;
import com.hashedin.redmask.config.MaskType;
import com.hashedin.redmask.config.MaskingConstants;
import com.hashedin.redmask.config.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This masking function masks an integer type column by a random number between the min
 * and max parameters.
 */
public class InRangeIntegerMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(InRangeIntegerMasking.class);

  private static final String PARAM_MINIMUM = "min";
  private static final String PARAM_MINIMUM__DEFAULT = "0";

  private static final String PARAM_MAXIMUM = "max";
  private static final String PARAM_MAXIMUM__DEFAULT = "10";

  private static final List<String> EXPECTED_PARAMETERS_LIST = new ArrayList<String>(
      Arrays.asList(PARAM_MAXIMUM, PARAM_MINIMUM));

  public InRangeIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public InRangeIntegerMasking() {
  }

  /**
   * The function add the masking function definition to the be created to the funcSet.
   *  @param config  TemplateConfiguration object to be used to create the function definition.
   * @param funcSet Set of function to be created to run the intended mask view.
   * @param dbType
   */
  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet,
                                    String dbType) {
    try {
      funcSet.add(MaskingQueryUtil.maskIntegerInRange(config, dbType));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      throw new RedmaskRuntimeException(String.format("Error occurred while adding MaskFunction"
          + " for Mask Type %s ", this.getMaskType()), ex);
    }
  }

  /**
   * This function is used to generate the SQL subquery that applies the intended mask onto
   * the column and add an alias as the original column name
   *
   * @param config    Template configuration in order to access the template used to create the
   *                  subquery.
   * @param tableName The name of the table.
   * @return The SubQuery designed specifically as per the mask and the masking parameters
   * provided by the user.
   * @throws RedmaskConfigException
   */
  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws RedmaskConfigException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_INTEGER_WITHIN_RANGE_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      throw new RedmaskRuntimeException(String.format("Error occurred while making SQL Sub query"
              + "for column  %s  in table %s for Mask Type %s ", this.getColumnName(),
          tableName, this.getMaskType()), ex);
    }
    return this.getColumnName();
  }

  /**
   * <p>
   * This function validates whether the correct parameter have been supplied by
   * the user in the configuration file. It also check whether each parameter has a valid value and
   * then adds these parameter in their respective order into the parameter list.
   * </p>
   * <p>
   * The Function will add the default value of the parameters value is not passed in the
   * maskparams config.
   * </p>
   *
   * @param parameters List of parameters required to create the intended mask.
   * @return The list of validated parameter
   * @throws RedmaskConfigException
   */
  private boolean validateAndAddParameters(List<String> parameters)
      throws RedmaskConfigException {
    for (String key : this.getMaskParams().keySet()) {
      if (!EXPECTED_PARAMETERS_LIST.contains(key)) {
        throw new RedmaskConfigException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }

    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.addAll(Arrays.asList(PARAM_MINIMUM__DEFAULT, PARAM_MAXIMUM__DEFAULT));
    }

    int min = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_MINIMUM, PARAM_MINIMUM__DEFAULT));
    int max = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_MAXIMUM, PARAM_MAXIMUM__DEFAULT));
    if (max > min) {
      parameters.add(String.valueOf(min));
      parameters.add(String.valueOf(max));
      return true;
    } else {
      throw new RedmaskConfigException(
          String.format("\'%s\' should be greater than \'%s\'", PARAM_MAXIMUM, PARAM_MINIMUM));
    }
  }
}
