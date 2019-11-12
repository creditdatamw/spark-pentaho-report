package com.creditdatamw.labs.kapenta.parameters;

import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.slf4j.LoggerFactory;

public class PentahoToParameterDefinitionMapper {
    /**
     * Map a pentaho report parameter to the ParameterDefinition class
     *
     * @param param
     * @param pentahoReport
     * @return
     */
    public ParameterDefinition map(ParameterDefinitionEntry param, MasterReport pentahoReport) {
        Object defaultValue = null;
        try {
            /**
             * Pentaho requires us to provide a {@link org.pentaho.reporting.engine.classic.core.parameters.ParameterContext}
             * which may fail since it needs to connect to the DataFactory (database)
             * for some other kinds of parameters so we allow the call to fail in which case
             * the default will remain a <code>null</code>
             */
            DefaultParameterContext context = new DefaultParameterContext(pentahoReport);
            defaultValue = param.getDefaultValue(context);
        } catch(Exception ex) {
            LoggerFactory.getLogger(getClass())
                .error("Failed to get default value for parameter", ex);
        }

        if (param.isMandatory()) {
            return ReportDefinition.requiredParameter(param.getName(),
                    defaultValue,
                    param.getValueType());
        }
        return ReportDefinition.optionalParameter(param.getName(),
                defaultValue,
                param.getValueType());
    }
}
