package org.hl7.fhir.validation.instance;

/*-
 * #%L
 * org.hl7.fhir.validation
 * %%
 * Copyright (C) 2014 - 2019 Health Level 7
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.convertors.*;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.exceptions.TerminologyServiceException;
import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.context.IWorkerContext.ValidationResult;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Element.SpecialElement;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.elementmodel.ObjectConverter;
import org.hl7.fhir.r5.elementmodel.ParserBase;
import org.hl7.fhir.r5.elementmodel.ParserBase.ValidationPolicy;
import org.hl7.fhir.r5.elementmodel.XmlParser;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.AggregationMode;
import org.hl7.fhir.r5.model.ElementDefinition.ConstraintSeverity;
import org.hl7.fhir.r5.model.ElementDefinition.DiscriminatorType;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionMappingComponent;
import org.hl7.fhir.r5.model.ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent;
import org.hl7.fhir.r5.model.ElementDefinition.PropertyRepresentation;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Enumeration;
import org.hl7.fhir.r5.model.Enumerations.BindingStrength;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.ExpressionNode;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.FhirPublication;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.InstantType;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r5.model.Range;
import org.hl7.fhir.r5.model.Ratio;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.SampledData;
import org.hl7.fhir.r5.model.SearchParameter;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureDefinition.ExtensionContextType;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionContextComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionSnapshotComponent;
import org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r5.model.TimeType;
import org.hl7.fhir.r5.model.Timing;
import org.hl7.fhir.r5.model.DataType;
import org.hl7.fhir.r5.model.TypeDetails;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.terminologies.ValueSetUtilities;
import org.hl7.fhir.r5.utils.FHIRLexer.FHIRLexerException;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.FHIRPathEngine.IEvaluationContext;
import org.hl7.fhir.r5.utils.IResourceValidator;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.I18nConstants;
import org.hl7.fhir.validation.BaseValidator;
import org.hl7.fhir.validation.instance.EnableWhenEvaluator.QStack;
import org.hl7.fhir.validation.XVerExtensionManager;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.Utilities.DecimalStatus;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.validation.ValidationOptions;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.validation.instance.utils.*;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ca.uhn.fhir.util.ObjectUtil;


/**
 * Thinking of using this in a java program? Don't!
 * You should use one of the wrappers instead. Either in HAPI, or use ValidationEngine, or NativeHostServices
 * <p>
 * Validation todo:
 * - support @default slices
 *
 * @author Grahame Grieve
 */
/*
 * todo:
 * check urn's don't start oid: or uuid:
 * check MetadataResource.url is absolute
 */

public class InstanceValidator extends BaseValidator implements IResourceValidator {

  private class ValidatorHostServices implements IEvaluationContext {

    @Override
    public Base resolveConstant(Object appContext, String name, boolean beforeContext) throws PathEngineException {
      ValidatorHostContext c = (ValidatorHostContext) appContext;
      if (externalHostServices != null)
        return externalHostServices.resolveConstant(c.getAppContext(), name, beforeContext);
      else
        return null;
    }

    @Override
    public TypeDetails resolveConstantType(Object appContext, String name) throws PathEngineException {
      ValidatorHostContext c = (ValidatorHostContext) appContext;
      if (externalHostServices != null)
        return externalHostServices.resolveConstantType(c.getAppContext(), name);
      else
        return null;
    }

    @Override
    public boolean log(String argument, List<Base> focus) {
      if (externalHostServices != null)
        return externalHostServices.log(argument, focus);
      else
        return false;
    }

    @Override
    public FunctionDetails resolveFunction(String functionName) {
      throw new Error(context.formatMessage(I18nConstants.NOT_DONE_YET_VALIDATORHOSTSERVICESRESOLVEFUNCTION_, functionName));
    }

    @Override
    public TypeDetails checkFunction(Object appContext, String functionName, List<TypeDetails> parameters) throws PathEngineException {
      throw new Error(context.formatMessage(I18nConstants.NOT_DONE_YET_VALIDATORHOSTSERVICESCHECKFUNCTION));
    }

    @Override
    public List<Base> executeFunction(Object appContext, String functionName, List<List<Base>> parameters) {
      throw new Error(context.formatMessage(I18nConstants.NOT_DONE_YET_VALIDATORHOSTSERVICESEXECUTEFUNCTION));
    }

    @Override
    public Base resolveReference(Object appContext, String url, Base refContext) throws FHIRException {
      ValidatorHostContext c = (ValidatorHostContext) appContext;

      if (refContext != null && refContext.hasUserData("validator.bundle.resolution")) {
        return (Base) refContext.getUserData("validator.bundle.resolution");
      }

      if (c.getAppContext() instanceof Element) {
        Element bnd = (Element) c.getAppContext();
        Base res = resolveInBundle(url, bnd);
        if (res != null)
          return res;
      }
      Base res = resolveInBundle(url, c.getResource());
      if (res != null)
        return res;
      res = resolveInBundle(url, c.getContainer());
      if (res != null)
        return res;

      if (externalHostServices != null)
        return externalHostServices.resolveReference(c.getAppContext(), url, refContext);
      else if (fetcher != null)
        try {
          return fetcher.fetch(c.getAppContext(), url);
        } catch (IOException e) {
          throw new FHIRException(e);
        }
      else
        throw new Error(context.formatMessage(I18nConstants.NOT_DONE_YET__RESOLVE__LOCALLY_2, url));

    }

    public Base resolveInBundle(String url, Element bnd) {
      if (bnd == null)
        return null;
      if (bnd.fhirType().equals("Bundle")) {
        for (Element be : bnd.getChildrenByName("entry")) {
          Element res = be.getNamedChild("resource");
          if (res != null) {
            String fullUrl = be.getChildValue("fullUrl");
            String rt = res.fhirType();
            String id = res.getChildValue("id");
            if (url.equals(fullUrl))
              return res;
            if (url.equals(rt + "/" + id))
              return res;
          }
        }
      }
      return null;
    }

    @Override
    public boolean conformsToProfile(Object appContext, Base item, String url) throws FHIRException {
      ValidatorHostContext ctxt = (ValidatorHostContext) appContext;
      StructureDefinition sd = context.fetchResource(StructureDefinition.class, url);
      if (sd == null) {
        throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_, url));
      }
      InstanceValidator self = InstanceValidator.this;
      List<ValidationMessage> valerrors = new ArrayList<ValidationMessage>();
      if (item instanceof Resource) {
        try {
          Element e = new ObjectConverter(context).convert((Resource) item);
          self.validateResource(new ValidatorHostContext(ctxt.getAppContext(), e), valerrors, e, e, sd, IdStatus.OPTIONAL, new NodeStack(e));
        } catch (IOException e1) {
          throw new FHIRException(e1);
        }
      } else if (item instanceof Element) {
        Element e = (Element) item;
        if (e.isResource()) {
          self.validateResource(new ValidatorHostContext(ctxt.getAppContext(), e), valerrors, e, e, sd, IdStatus.OPTIONAL, new NodeStack(e));
        } else {
          throw new FHIRException(context.formatMessage(I18nConstants.NOT_SUPPORTED_YET));
        }
      } else
        throw new NotImplementedException(context.formatMessage(I18nConstants.NOT_DONE_YET_VALIDATORHOSTSERVICESCONFORMSTOPROFILE_WHEN_ITEM_IS_NOT_AN_ELEMENT));
      boolean ok = true;
      List<ValidationMessage> record = new ArrayList<>();
      for (ValidationMessage v : valerrors) {
        ok = ok && !v.getLevel().isError();
        if (v.getLevel().isError() || v.isSlicingHint()) {
          record.add(v);
        }
      }
      if (!ok && !record.isEmpty()) {
        ctxt.sliceNotes(url, record);
      }
      return ok;
    }

    @Override
    public ValueSet resolveValueSet(Object appContext, String url) {
      ValidatorHostContext c = (ValidatorHostContext) appContext;
      if (c.getProfile() != null && url.startsWith("#")) {
        for (Resource r : c.getProfile().getContained()) {
          if (r.getId().equals(url.substring(1))) {
            if (r instanceof ValueSet)
              return (ValueSet) r;
            else
              throw new FHIRException(context.formatMessage(I18nConstants.REFERENCE__REFERS_TO_A__NOT_A_VALUESET, url, r.fhirType()));
          }
        }
        return null;
      }
      return context.fetchResource(ValueSet.class, url);
    }

  }

  private IWorkerContext context;
  private FHIRPathEngine fpe;

  // configuration items
  private CheckDisplayOption checkDisplay;
  private boolean anyExtensionsAllowed;
  private boolean errorForUnknownProfiles;
  private boolean noInvariantChecks;
  private boolean noTerminologyChecks;
  private boolean hintAboutNonMustSupport;
  private boolean showMessagesFromReferences;
  private BestPracticeWarningLevel bpWarnings;
  private String validationLanguage;
  private boolean baseOnly;

  private List<String> extensionDomains = new ArrayList<String>();

  private IdStatus resourceIdRule;
  private boolean allowXsiLocation;

  // used during the build process to keep the overall volume of messages down
  private boolean suppressLoincSnomedMessages;

  // time tracking
  private long overall = 0;
  private long txTime = 0;
  private long sdTime = 0;
  private long loadTime = 0;
  private long fpeTime = 0;

  private boolean noBindingMsgSuppressed;
  private boolean debug;
  private Map<String, Element> fetchCache = new HashMap<>();
  private HashMap<Element, ResourceValidationTracker> resourceTracker = new HashMap<>();
  private IValidatorResourceFetcher fetcher;
  long time = 0;
  private IEvaluationContext externalHostServices;
  private boolean noExtensibleWarnings;
  private String serverBase;

  private EnableWhenEvaluator myEnableWhenEvaluator = new EnableWhenEvaluator();
  private String executionId;
  private XVerExtensionManager xverManager;
  private IValidationProfileUsageTracker tracker;
  private ValidatorHostServices validatorServices;
  private boolean assumeValidRestReferences;
  private boolean allowExamples;
  private ProfileUtilities profileUtilities;

  public InstanceValidator(IWorkerContext theContext, IEvaluationContext hostServices) {
    super(theContext);
    this.context = theContext;
    this.externalHostServices = hostServices;
    this.profileUtilities = new ProfileUtilities(theContext, null, null);
    fpe = new FHIRPathEngine(context);
    validatorServices = new ValidatorHostServices();
    fpe.setHostServices(validatorServices);
    if (theContext.getVersion().startsWith("3.0") || theContext.getVersion().startsWith("1.0"))
      fpe.setLegacyMode(true);
    source = Source.InstanceValidator;
  }

  @Override
  public boolean isNoExtensibleWarnings() {
    return noExtensibleWarnings;
  }

  @Override
  public IResourceValidator setNoExtensibleWarnings(boolean noExtensibleWarnings) {
    this.noExtensibleWarnings = noExtensibleWarnings;
    return this;
  }

  @Override
  public boolean isShowMessagesFromReferences() {
    return showMessagesFromReferences;
  }

  @Override
  public void setShowMessagesFromReferences(boolean showMessagesFromReferences) {
    this.showMessagesFromReferences = showMessagesFromReferences;
  }

  @Override
  public boolean isNoInvariantChecks() {
    return noInvariantChecks;
  }

  @Override
  public IResourceValidator setNoInvariantChecks(boolean value) {
    this.noInvariantChecks = value;
    return this;
  }

  public IValidatorResourceFetcher getFetcher() {
    return this.fetcher;
  }

  public IResourceValidator setFetcher(IValidatorResourceFetcher value) {
    this.fetcher = value;
    return this;
  }

  public IValidationProfileUsageTracker getTracker() {
    return this.tracker;
  }

  public IResourceValidator setTracker(IValidationProfileUsageTracker value) {
    this.tracker = value;
    return this;
  }


  public boolean isHintAboutNonMustSupport() {
    return hintAboutNonMustSupport;
  }

  public void setHintAboutNonMustSupport(boolean hintAboutNonMustSupport) {
    this.hintAboutNonMustSupport = hintAboutNonMustSupport;
  }

  public boolean isAssumeValidRestReferences() {
    return this.assumeValidRestReferences;
  }

  public void setAssumeValidRestReferences(boolean value) {
    this.assumeValidRestReferences = value;
  }

  public boolean isAllowExamples() {
    return this.allowExamples;
  }

  public void setAllowExamples(boolean value) {
    this.allowExamples = value;
  }


  private boolean allowUnknownExtension(String url) {
    if ((allowExamples && (url.contains("example.org") || url.contains("acme.com"))) || url.contains("nema.org") || url.startsWith("http://hl7.org/fhir/tools/StructureDefinition/") || url.equals("http://hl7.org/fhir/StructureDefinition/structuredefinition-expression"))
      // Added structuredefinition-expression explicitly because it wasn't defined in the version of the spec it needs to be used with
      return true;
    for (String s : extensionDomains)
      if (url.startsWith(s))
        return true;
    return anyExtensionsAllowed;
  }

  private boolean isKnownExtension(String url) {
    // Added structuredefinition-expression and following extensions explicitly because they weren't defined in the version of the spec they need to be used with
    if ((allowExamples && (url.contains("example.org") || url.contains("acme.com"))) || url.contains("nema.org") || url.startsWith("http://hl7.org/fhir/tools/StructureDefinition/") || url.equals("http://hl7.org/fhir/StructureDefinition/structuredefinition-expression") || url.equals(VersionConvertorConstants.IG_DEPENDSON_PACKAGE_EXTENSION))
      return true;
    for (String s : extensionDomains)
      if (url.startsWith(s))
        return true;
    return false;
  }

  private void bpCheck(List<ValidationMessage> errors, IssueType invalid, int line, int col, String literalPath, boolean test, String message) {
    if (bpWarnings != null) {
      switch (bpWarnings) {
        case Error:
          rule(errors, invalid, line, col, literalPath, test, message);
          break;
        case Warning:
          warning(errors, invalid, line, col, literalPath, test, message);
          break;
        case Hint:
          hint(errors, invalid, line, col, literalPath, test, message);
          break;
        default: // do nothing
          break;
      }
    }
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format) throws FHIRException {
    return validate(appContext, errors, stream, format, new ArrayList<>());
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    return validate(appContext, errors, stream, format, profiles);
  }

  private StructureDefinition getSpecifiedProfile(String profile) {
    StructureDefinition sd = context.fetchResource(StructureDefinition.class, profile);
    if (sd == null) {
      throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_LOCATE_THE_PROFILE__IN_ORDER_TO_VALIDATE_AGAINST_IT, profile));
    }
    return sd;
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, InputStream stream, FhirFormat format, List<StructureDefinition> profiles) throws FHIRException {
    ParserBase parser = Manager.makeParser(context, format);
    if (parser instanceof XmlParser)
      ((XmlParser) parser).setAllowXsiLocation(allowXsiLocation);
    parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
    long t = System.nanoTime();
    Element e;
    try {
      e = parser.parse(stream);
    } catch (IOException e1) {
      throw new FHIRException(e1);
    }
    loadTime = System.nanoTime() - t;
    if (e != null)
      validate(appContext, errors, e, profiles);
    return e;
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Resource resource) throws FHIRException {
    return validate(appContext, errors, resource, new ArrayList<>());
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Resource resource, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    return validate(appContext, errors, resource, profiles);
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Resource resource, List<StructureDefinition> profiles) throws FHIRException {
    long t = System.nanoTime();
    Element e;
    try {
      e = new ObjectConverter(context).convert(resource);
    } catch (IOException e1) {
      throw new FHIRException(e1);
    }
    loadTime = System.nanoTime() - t;
    validate(appContext, errors, e, profiles);
    return e;
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element) throws FHIRException {
    return validate(appContext, errors, element, new ArrayList<>());
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    return validate(appContext, errors, element, profiles);
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, org.w3c.dom.Element element, List<StructureDefinition> profiles) throws FHIRException {
    XmlParser parser = new XmlParser(context);
    parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
    long t = System.nanoTime();
    Element e;
    try {
      e = parser.parse(element);
    } catch (IOException e1) {
      throw new FHIRException(e1);
    }
    loadTime = System.nanoTime() - t;
    if (e != null)
      validate(appContext, errors, e, profiles);
    return e;
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Document document) throws FHIRException {
    return validate(appContext, errors, document, new ArrayList<>());
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Document document, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    return validate(appContext, errors, document, profiles);
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, Document document, List<StructureDefinition> profiles) throws FHIRException {
    XmlParser parser = new XmlParser(context);
    parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
    long t = System.nanoTime();
    Element e;
    try {
      e = parser.parse(document);
    } catch (IOException e1) {
      throw new FHIRException(e1);
    }
    loadTime = System.nanoTime() - t;
    if (e != null)
      validate(appContext, errors, e, profiles);
    return e;
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object) throws FHIRException {
    return validate(appContext, errors, object, new ArrayList<>());
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    return validate(appContext, errors, object, profiles);
  }

  @Override
  public org.hl7.fhir.r5.elementmodel.Element validate(Object appContext, List<ValidationMessage> errors, JsonObject object, List<StructureDefinition> profiles) throws FHIRException {
    JsonParser parser = new JsonParser(context);
    parser.setupValidation(ValidationPolicy.EVERYTHING, errors);
    long t = System.nanoTime();
    Element e = parser.parse(object);
    loadTime = System.nanoTime() - t;
    if (e != null)
      validate(appContext, errors, e, profiles);
    return e;
  }

  @Override
  public void validate(Object appContext, List<ValidationMessage> errors, Element element) throws FHIRException {
    validate(appContext, errors, element, new ArrayList<>());
  }

  @Override
  public void validate(Object appContext, List<ValidationMessage> errors, Element element, String profile) throws FHIRException {
    ArrayList<StructureDefinition> profiles = new ArrayList<>();
    if (profile != null) {
      profiles.add(getSpecifiedProfile(profile));
    }
    validate(appContext, errors, element, profiles);
  }

  @Override
  public void validate(Object appContext, List<ValidationMessage> errors, Element element, List<StructureDefinition> profiles) throws FHIRException {
    // this is the main entry point; all the other public entry points end up here coming here...
    // so the first thing to do is to clear the internal state
    fetchCache.clear();
    fetchCache.put(element.fhirType() + "/" + element.getIdBase(), element);
    resourceTracker.clear();
    executionId = UUID.randomUUID().toString();
    baseOnly = profiles.isEmpty();

    long t = System.nanoTime();
    if (profiles == null || profiles.isEmpty()) {
      validateResource(new ValidatorHostContext(appContext, element), errors, element, element, null, resourceIdRule, new NodeStack(element));
    } else {
      for (StructureDefinition defn : profiles) {
        validateResource(new ValidatorHostContext(appContext, element), errors, element, element, defn, resourceIdRule, new NodeStack(element));
      }
    }
    if (hintAboutNonMustSupport) {
      checkElementUsage(errors, element, new NodeStack(element));
    }
    overall = System.nanoTime() - t;
  }

  private void checkElementUsage(List<ValidationMessage> errors, Element element, NodeStack stack) {
    String elementUsage = element.getUserString("elementSupported");
    hint(errors, IssueType.INFORMATIONAL, element.line(), element.col(), stack.getLiteralPath(), elementUsage == null || elementUsage.equals("Y"), I18nConstants.MUSTSUPPORT_VAL_MUSTSUPPORT, element.getName(), element.getProperty().getStructure().getUrl());

    if (element.hasChildren()) {
      String prevName = "";
      int elementCount = 0;
      for (Element ce : element.getChildren()) {
        if (ce.getName().equals(prevName))
          elementCount++;
        else {
          elementCount = 1;
          prevName = ce.getName();
        }
        checkElementUsage(errors, ce, stack.push(ce, elementCount, null, null));
      }
    }
  }

  private boolean check(String v1, String v2) {
    return v1 == null ? Utilities.noString(v1) : v1.equals(v2);
  }

  private void checkAddress(List<ValidationMessage> errors, String path, Element focus, Address fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern);
    checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern);
    checkFixedValue(errors, path + ".city", focus.getNamedChild("city"), fixed.getCityElement(), fixedSource, "city", focus, pattern);
    checkFixedValue(errors, path + ".state", focus.getNamedChild("state"), fixed.getStateElement(), fixedSource, "state", focus, pattern);
    checkFixedValue(errors, path + ".country", focus.getNamedChild("country"), fixed.getCountryElement(), fixedSource, "country", focus, pattern);
    checkFixedValue(errors, path + ".zip", focus.getNamedChild("zip"), fixed.getPostalCodeElement(), fixedSource, "postalCode", focus, pattern);

    List<Element> lines = new ArrayList<Element>();
    focus.getNamedChildren("line", lines);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, lines.size() == fixed.getLine().size(), I18nConstants.FIXED_TYPE_CHECKS_DT_ADDRESS_LINE, Integer.toString(fixed.getLine().size()), Integer.toString(lines.size()))) {
      for (int i = 0; i < lines.size(); i++)
        checkFixedValue(errors, path + ".coding", lines.get(i), fixed.getLine().get(i), fixedSource, "coding", focus, pattern);
    }
  }

  private void checkAttachment(List<ValidationMessage> errors, String path, Element focus, Attachment fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".contentType", focus.getNamedChild("contentType"), fixed.getContentTypeElement(), fixedSource, "contentType", focus, pattern);
    checkFixedValue(errors, path + ".language", focus.getNamedChild("language"), fixed.getLanguageElement(), fixedSource, "language", focus, pattern);
    checkFixedValue(errors, path + ".data", focus.getNamedChild("data"), fixed.getDataElement(), fixedSource, "data", focus, pattern);
    checkFixedValue(errors, path + ".url", focus.getNamedChild("url"), fixed.getUrlElement(), fixedSource, "url", focus, pattern);
    checkFixedValue(errors, path + ".size", focus.getNamedChild("size"), fixed.getSizeElement(), fixedSource, "size", focus, pattern);
    checkFixedValue(errors, path + ".hash", focus.getNamedChild("hash"), fixed.getHashElement(), fixedSource, "hash", focus, pattern);
    checkFixedValue(errors, path + ".title", focus.getNamedChild("title"), fixed.getTitleElement(), fixedSource, "title", focus, pattern);
  }

  // public API
  private boolean checkCode(List<ValidationMessage> errors, Element element, String path, String code, String system, String display, boolean checkDisplay, NodeStack stack) throws TerminologyServiceException {
    long t = System.nanoTime();
    boolean ss = context.supportsSystem(system);
    txTime = txTime + (System.nanoTime() - t);
    if (ss) {
      t = System.nanoTime();
      ValidationResult s = context.validateCode(new ValidationOptions(stack.workingLang), system, code, checkDisplay ? display : null);
      txTime = txTime + (System.nanoTime() - t);
      if (s == null)
        return true;
      if (s.isOk()) {
        if (s.getMessage() != null)
          txWarning(errors, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage());
        return true;
      }
      if (s.getErrorClass() != null && s.getErrorClass().isInfrastructure())
        txWarning(errors, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage());
      else if (s.getSeverity() == IssueSeverity.INFORMATION)
        txHint(errors, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage());
      else if (s.getSeverity() == IssueSeverity.WARNING)
        txWarning(errors, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, s.getMessage());
      else
        return txRule(errors, s.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, s == null, I18nConstants.TERMINOLOGY_PASSTHROUGH_TX_MESSAGE, s.getMessage(), system, code);
      return true;
    } else if (system.startsWith("http://hl7.org/fhir")) {
      if (Utilities.existsInList(system, "http://hl7.org/fhir/sid/icd-10", "http://hl7.org/fhir/sid/cvx", "http://hl7.org/fhir/sid/icd-10", "http://hl7.org/fhir/sid/icd-10-cm", "http://hl7.org/fhir/sid/icd-9", "http://hl7.org/fhir/sid/ndc", "http://hl7.org/fhir/sid/srt"))
        return true; // else don't check these (for now)
      else if (system.startsWith("http://hl7.org/fhir/test"))
        return true; // we don't validate these
      else {
        CodeSystem cs = getCodeSystem(system);
        if (rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, cs != null, I18nConstants.TERMINOLOGY_TX_SYSTEM_UNKNOWN, system)) {
          ConceptDefinitionComponent def = getCodeDefinition(cs, code);
          if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, def != null, I18nConstants.TERMINOLOGY_TX_CODE_UNKNOWN, system, code))
            return warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, display == null || display.equals(def.getDisplay()), I18nConstants.TERMINOLOGY_TX_DISPLAY_WRONG, def.getDisplay());
        }
        return false;
      }
    } else if (context.isNoTerminologyServer() && Utilities.existsInList(system, "http://loinc.org", "http://unitsofmeasure.org", "http://snomed.info/sct", "http://www.nlm.nih.gov/research/umls/rxnorm")) {
      return true; // no checks in this case
    } else if (startsWithButIsNot(system, "http://snomed.info/sct", "http://loinc.org", "http://unitsofmeasure.org", "http://www.nlm.nih.gov/research/umls/rxnorm")) {
      rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_SYSTEM_INVALID, system);
      return false;
    } else {
      try {
        if (context.fetchResourceWithException(ValueSet.class, system) != null) {
          rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_SYSTEM_VALUESET, system);
          // Lloyd: This error used to prohibit checking for downstream issues, but there are some cases where that checking needs to occur.  Please talk to me before changing the code back.
        }
        hint(errors, IssueType.UNKNOWN, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_SYSTEM_NOTKNOWN, system);
        return true;
      } catch (Exception e) {
        return true;
      }
    }
  }

  private boolean startsWithButIsNot(String system, String... uri) {
    for (String s : uri)
      if (!system.equals(s) && system.startsWith(s))
        return true;
    return false;
  }


  private boolean hasErrors(List<ValidationMessage> errors) {
    if (errors != null) {
      for (ValidationMessage vm : errors) {
        if (vm.getLevel() == IssueSeverity.FATAL || vm.getLevel() == IssueSeverity.ERROR) {
          return true;
        }
      }
    }
    return false;
  }

  private void checkCodeableConcept(List<ValidationMessage> errors, String path, Element focus, CodeableConcept fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern);
    List<Element> codings = new ArrayList<Element>();
    focus.getNamedChildren("coding", codings);
    if (pattern) {
      if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, codings.size() >= fixed.getCoding().size(), I18nConstants.TERMINOLOGY_TX_CODING_COUNT, Integer.toString(fixed.getCoding().size()), Integer.toString(codings.size()))) {
        for (int i = 0; i < fixed.getCoding().size(); i++) {
          Coding fixedCoding = fixed.getCoding().get(i);
          boolean found = false;
          List<ValidationMessage> allErrorsFixed = new ArrayList<>();
          List<ValidationMessage> errorsFixed;
          for (int j = 0; j < codings.size() && !found; ++j) {
            errorsFixed = new ArrayList<>();
            checkFixedValue(errorsFixed, path + ".coding", codings.get(j), fixedCoding, fixedSource, "coding", focus, pattern);
            if (!hasErrors(errorsFixed)) {
              found = true;
            } else {
              errorsFixed
                .stream()
                .filter(t -> t.getLevel().ordinal() >= IssueSeverity.ERROR.ordinal())
                .forEach(t -> allErrorsFixed.add(t));
            }
          }
          if (!found) {
            // The argonaut DSTU2 labs profile requires userSelected=false on the category.coding and this
            // needs to produce an understandable error message
            String message = "Expected CodeableConcept " + (pattern ? "pattern" : "fixed value") + " not found for" +
              " system: " + fixedCoding.getSystemElement().asStringValue() +
              " code: " + fixedCoding.getCodeElement().asStringValue() +
              " display: " + fixedCoding.getDisplayElement().asStringValue();
            if (fixedCoding.hasUserSelected()) {
              message += " userSelected: " + fixedCoding.getUserSelected();
            }
            message += " - Issues: " + allErrorsFixed;
            rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, false, message);
          }
        }
      }
    } else {
      if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, codings.size() == fixed.getCoding().size(), I18nConstants.TERMINOLOGY_TX_CODING_COUNT, Integer.toString(fixed.getCoding().size()), Integer.toString(codings.size()))) {
        for (int i = 0; i < codings.size(); i++)
          checkFixedValue(errors, path + ".coding", codings.get(i), fixed.getCoding().get(i), fixedSource, "coding", focus);
      }
    }
  }

  private boolean checkCodeableConcept(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition theElementCntext, NodeStack stack) {
    boolean res = true;
    if (!noTerminologyChecks && theElementCntext != null && theElementCntext.hasBinding()) {
      ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
      if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, I18nConstants.TERMINOLOGY_TX_BINDING_MISSING, path)) {
        if (binding.hasValueSet()) {
          ValueSet valueset = resolveBindingReference(profile, binding.getValueSet(), profile.getUrl());
          if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(binding.getValueSet()))) {
            try {
              CodeableConcept cc = ObjectConverter.readAsCodeableConcept(element);
              if (!cc.hasCoding()) {
                if (binding.getStrength() == BindingStrength.REQUIRED)
                  rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_VALUESET, describeReference(binding.getValueSet()), valueset.getUrl());
                else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                  if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                    rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_VALUESETMAX, describeReference(ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")), valueset.getUrl());
                  else
                    warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_VALUESET_EXT, describeReference(binding.getValueSet()), valueset.getUrl());
                }
              } else {
                long t = System.nanoTime();

                // Check whether the codes are appropriate for the type of binding we have
                boolean bindingsOk = true;
                if (binding.getStrength() != BindingStrength.EXAMPLE) {
                  boolean atLeastOneSystemIsSupported = false;
                  for (Coding nextCoding : cc.getCoding()) {
                    String nextSystem = nextCoding.getSystem();
                    if (isNotBlank(nextSystem) && context.supportsSystem(nextSystem)) {
                      atLeastOneSystemIsSupported = true;
                      break;
                    }
                  }

                  if (!atLeastOneSystemIsSupported && binding.getStrength() == BindingStrength.EXAMPLE) {
                    // ignore this since we can't validate but it doesn't matter..
                  } else {
                    ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang).checkValueSetOnly(), cc, valueset); // we're going to validate the codings directly, so only check the valueset
                    if (!vr.isOk()) {
                      bindingsOk = false;
                      if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_1, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), cc, stack);
                          else if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_2, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_3, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                          }
                        }
                      } else {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_1, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), cc, stack);
                          if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_2, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_3, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                          }
                        }
                      }
                    } else if (vr.getMessage() != null) {
                      res = false;
                      txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage());
                    } else {
                      res = false;
                    }
                  }
                  // Then, for any codes that are in code systems we are able
                  // to validate, we'll validate that the codes actually exist
                  if (bindingsOk) {
                    for (Coding nextCoding : cc.getCoding()) {
                      if (isNotBlank(nextCoding.getCode()) && isNotBlank(nextCoding.getSystem()) && context.supportsSystem(nextCoding.getSystem())) {
                        ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang).noCheckValueSetMembership(), nextCoding, valueset);
                        if (vr.getSeverity() != null) {
                          if (vr.getSeverity() == IssueSeverity.INFORMATION) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage());
                          } else if (vr.getSeverity() == IssueSeverity.WARNING) {
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage());
                          } else {
                            txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage());
                          }
                        }
                      }
                    }
                  }
                  txTime = txTime + (System.nanoTime() - t);
                }
              }
            } catch (Exception e) {
              warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODEABLECONCEPT, e.getMessage());
            }
          }
        } else if (binding.hasValueSet()) {
          hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_CANTCHECK);
        } else if (!noBindingMsgSuppressed) {
          hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSOURCE, path);
        }
      }
    }
    return res;
  }

  private boolean checkTerminologyCodeableConcept(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition theElementCntext, NodeStack stack, StructureDefinition logical) {
    boolean res = true;
    if (!noTerminologyChecks && theElementCntext != null && theElementCntext.hasBinding()) {
      ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
      if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, I18nConstants.TERMINOLOGY_TX_BINDING_MISSING, path)) {
        if (binding.hasValueSet()) {
          ValueSet valueset = resolveBindingReference(profile, binding.getValueSet(), profile.getUrl());
          if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(binding.getValueSet()))) {
            try {
              CodeableConcept cc = convertToCodeableConcept(element, logical);
              if (!cc.hasCoding()) {
                if (binding.getStrength() == BindingStrength.REQUIRED)
                  rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, "No code provided, and a code is required from the value set " + describeReference(binding.getValueSet()) + " (" + valueset.getUrl());
                else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                  if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                    rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_VALUESETMAX, describeReference(ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet")), valueset.getUrl());
                  else
                    warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_VALUESET_EXT, describeReference(binding.getValueSet()), valueset.getUrl());
                }
              } else {
                long t = System.nanoTime();

                // Check whether the codes are appropriate for the type of binding we have
                boolean bindingsOk = true;
                if (binding.getStrength() != BindingStrength.EXAMPLE) {
                  boolean atLeastOneSystemIsSupported = false;
                  for (Coding nextCoding : cc.getCoding()) {
                    String nextSystem = nextCoding.getSystem();
                    if (isNotBlank(nextSystem) && context.supportsSystem(nextSystem)) {
                      atLeastOneSystemIsSupported = true;
                      break;
                    }
                  }

                  if (!atLeastOneSystemIsSupported && binding.getStrength() == BindingStrength.EXAMPLE) {
                    // ignore this since we can't validate but it doesn't matter..
                  } else {
                    ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang), cc, valueset); // we're going to validate the codings directly
                    if (!vr.isOk()) {
                      bindingsOk = false;
                      if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure()) {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_1, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), cc, stack);
                          else if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_2, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_3, describeReference(binding.getValueSet()), vr.getErrorClass().toString());
                          }
                        }
                      } else {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_1, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), cc, stack);
                          if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_2, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_3, describeReference(binding.getValueSet()), valueset.getUrl(), ccSummary(cc));
                          }
                        }
                      }
                    } else if (vr.getMessage() != null) {
                      res = false;
                      txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, vr.getMessage());
                    } else {
                      res = false;
                    }
                  }
                  // Then, for any codes that are in code systems we are able
                  // to validate, we'll validate that the codes actually exist
                  if (bindingsOk) {
                    for (Coding nextCoding : cc.getCoding()) {
                      String nextCode = nextCoding.getCode();
                      String nextSystem = nextCoding.getSystem();
                      if (isNotBlank(nextCode) && isNotBlank(nextSystem) && context.supportsSystem(nextSystem)) {
                        ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang), nextSystem, nextCode, null);
                        if (!vr.isOk()) {
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CODE_NOTVALID, nextCode, nextSystem);
                        }
                      }
                    }
                  }
                  txTime = txTime + (System.nanoTime() - t);
                }
              }
            } catch (Exception e) {
              warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODEABLECONCEPT, e.getMessage());
            }
            // special case: if the logical model has both CodeableConcept and Coding mappings, we'll also check the first coding.
            if (getMapping("http://hl7.org/fhir/terminology-pattern", logical, logical.getSnapshot().getElementFirstRep()).contains("Coding")) {
              checkTerminologyCoding(errors, path, element, profile, theElementCntext, true, true, stack, logical);
            }
          }
        } else if (binding.hasValueSet()) {
          hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_CANTCHECK);
        } else if (!noBindingMsgSuppressed) {
          hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSOURCE, path);
        }
      }
    }
    return res;
  }

  private void checkTerminologyCoding(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition theElementCntext, boolean inCodeableConcept, boolean checkDisplay, NodeStack stack, StructureDefinition logical) {
    Coding c = convertToCoding(element, logical);
    String code = c.getCode();
    String system = c.getSystem();
    String display = c.getDisplay();
    rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, isAbsolute(system), I18nConstants.TERMINOLOGY_TX_SYSTEM_RELATIVE);

    if (system != null && code != null && !noTerminologyChecks) {
      rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, !isValueSet(system), I18nConstants.TERMINOLOGY_TX_SYSTEM_VALUESET2, system);
      try {
        if (checkCode(errors, element, path, code, system, display, checkDisplay, stack))
          if (theElementCntext != null && theElementCntext.hasBinding()) {
            ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
            if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, I18nConstants.TERMINOLOGY_TX_BINDING_MISSING2, path)) {
              if (binding.hasValueSet()) {
                ValueSet valueset = resolveBindingReference(profile, binding.getValueSet(), profile.getUrl());
                if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(binding.getValueSet()))) {
                  try {
                    long t = System.nanoTime();
                    ValidationResult vr = null;
                    if (binding.getStrength() != BindingStrength.EXAMPLE) {
                      vr = context.validateCode(new ValidationOptions(stack.workingLang), c, valueset);
                    }
                    txTime = txTime + (System.nanoTime() - t);
                    if (vr != null && !vr.isOk()) {
                      if (vr.IsNoService())
                        txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSERVER);
                      else if (vr.getErrorClass() != null && !vr.getErrorClass().isInfrastructure()) {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_4, describeReference(binding.getValueSet(), valueset));
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), c, stack);
                          else if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_5, describeReference(binding.getValueSet(), valueset));
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_6, describeReference(binding.getValueSet(), valueset));
                          }
                        }
                      } else if (binding.getStrength() == BindingStrength.REQUIRED)
                        txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_4, describeReference(binding.getValueSet(), valueset), (vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : ""));
                      else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                        if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                          checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), c, stack);
                        else
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_5, describeReference(binding.getValueSet(), valueset), (vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : ""));
                      } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                        if (baseOnly) {
                          txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_6, describeReference(binding.getValueSet(), valueset), (vr.getMessage() != null ? " (error message = " + vr.getMessage() + ")" : ""));
                        }
                      }
                    }
                  } catch (Exception e) {
                    warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODING1, e.getMessage());
                  }
                }
              } else if (binding.hasValueSet()) {
                hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_CANTCHECK);
              } else if (!inCodeableConcept && !noBindingMsgSuppressed) {
                hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSOURCE, path);
              }
            }
          }
      } catch (Exception e) {
        rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODING2, e.getMessage(), e.toString());
      }
    }
  }

  private CodeableConcept convertToCodeableConcept(Element element, StructureDefinition logical) {
    CodeableConcept res = new CodeableConcept();
    for (ElementDefinition ed : logical.getSnapshot().getElement()) {
      if (Utilities.charCount(ed.getPath(), '.') == 1) {
        List<String> maps = getMapping("http://hl7.org/fhir/terminology-pattern", logical, ed);
        for (String m : maps) {
          String name = tail(ed.getPath());
          List<Element> list = new ArrayList<>();
          element.getNamedChildren(name, list);
          if (!list.isEmpty()) {
            if ("Coding.code".equals(m)) {
              res.getCodingFirstRep().setCode(list.get(0).primitiveValue());
            } else if ("Coding.system[fmt:OID]".equals(m)) {
              String oid = list.get(0).primitiveValue();
              String url = context.oid2Uri(oid);
              if (url != null) {
                res.getCodingFirstRep().setSystem(url);
              } else {
                res.getCodingFirstRep().setSystem("urn:oid:" + oid);
              }
            } else if ("Coding.version".equals(m)) {
              res.getCodingFirstRep().setVersion(list.get(0).primitiveValue());
            } else if ("Coding.display".equals(m)) {
              res.getCodingFirstRep().setDisplay(list.get(0).primitiveValue());
            } else if ("CodeableConcept.text".equals(m)) {
              res.setText(list.get(0).primitiveValue());
            } else if ("CodeableConcept.coding".equals(m)) {
              StructureDefinition c = context.fetchTypeDefinition(ed.getTypeFirstRep().getCode());
              for (Element e : list) {
                res.addCoding(convertToCoding(e, c));
              }
            }
          }
        }
      }
    }
    return res;
  }

  private Coding convertToCoding(Element element, StructureDefinition logical) {
    Coding res = new Coding();
    for (ElementDefinition ed : logical.getSnapshot().getElement()) {
      if (Utilities.charCount(ed.getPath(), '.') == 1) {
        List<String> maps = getMapping("http://hl7.org/fhir/terminology-pattern", logical, ed);
        for (String m : maps) {
          String name = tail(ed.getPath());
          List<Element> list = new ArrayList<>();
          element.getNamedChildren(name, list);
          if (!list.isEmpty()) {
            if ("Coding.code".equals(m)) {
              res.setCode(list.get(0).primitiveValue());
            } else if ("Coding.system[fmt:OID]".equals(m)) {
              String oid = list.get(0).primitiveValue();
              String url = context.oid2Uri(oid);
              if (url != null) {
                res.setSystem(url);
              } else {
                res.setSystem("urn:oid:" + oid);
              }
            } else if ("Coding.version".equals(m)) {
              res.setVersion(list.get(0).primitiveValue());
            } else if ("Coding.display".equals(m)) {
              res.setDisplay(list.get(0).primitiveValue());
            }
          }
        }
      }
    }
    return res;
  }

  private void checkMaxValueSet(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, CodeableConcept cc, NodeStack stack) {
    // TODO Auto-generated method stub
    ValueSet valueset = resolveBindingReference(profile, maxVSUrl, profile.getUrl());
    if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(maxVSUrl))) {
      try {
        long t = System.nanoTime();
        ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang), cc, valueset);
        txTime = txTime + (System.nanoTime() - t);
        if (!vr.isOk()) {
          if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure())
            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_7, describeReference(maxVSUrl), valueset.getUrl(), vr.getMessage());
          else
            txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_8, describeReference(maxVSUrl), valueset.getUrl(), ccSummary(cc));
        }
      } catch (Exception e) {
        warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODEABLECONCEPT_MAX, e.getMessage());
      }
    }
  }

  private void checkMaxValueSet(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, Coding c, NodeStack stack) {
    // TODO Auto-generated method stub
    ValueSet valueset = resolveBindingReference(profile, maxVSUrl, profile.getUrl());
    if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(maxVSUrl))) {
      try {
        long t = System.nanoTime();
        ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang), c, valueset);
        txTime = txTime + (System.nanoTime() - t);
        if (!vr.isOk()) {
          if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure())
            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_9, describeReference(maxVSUrl), valueset.getUrl(), vr.getMessage());
          else
            txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_10, describeReference(maxVSUrl), valueset.getUrl(), c.getSystem(), c.getCode());
        }
      } catch (Exception e) {
        warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODEABLECONCEPT_MAX, e.getMessage());
      }
    }
  }

  private void checkMaxValueSet(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, String maxVSUrl, String value, NodeStack stack) {
    // TODO Auto-generated method stub
    ValueSet valueset = resolveBindingReference(profile, maxVSUrl, profile.getUrl());
    if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(maxVSUrl))) {
      try {
        long t = System.nanoTime();
        ValidationResult vr = context.validateCode(new ValidationOptions(stack.workingLang), value, valueset);
        txTime = txTime + (System.nanoTime() - t);
        if (!vr.isOk()) {
          if (vr.getErrorClass() != null && vr.getErrorClass().isInfrastructure())
            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_9, describeReference(maxVSUrl), valueset.getUrl(), vr.getMessage());
          else
            txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_11, describeReference(maxVSUrl), valueset.getUrl(), "), and a code from this value set is required) (code = " + value + "), (error = " + vr.getMessage() + ")");
        }
      } catch (Exception e) {
        warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODEABLECONCEPT_MAX, e.getMessage());
      }
    }
  }

  private String ccSummary(CodeableConcept cc) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (Coding c : cc.getCoding())
      b.append(c.getSystem() + "#" + c.getCode());
    return b.toString();
  }

  private void checkCoding(List<ValidationMessage> errors, String path, Element focus, Coding fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern);
    checkFixedValue(errors, path + ".version", focus.getNamedChild("version"), fixed.getVersionElement(), fixedSource, "version", focus, pattern);
    checkFixedValue(errors, path + ".code", focus.getNamedChild("code"), fixed.getCodeElement(), fixedSource, "code", focus, pattern);
    checkFixedValue(errors, path + ".display", focus.getNamedChild("display"), fixed.getDisplayElement(), fixedSource, "display", focus, pattern);
    checkFixedValue(errors, path + ".userSelected", focus.getNamedChild("userSelected"), fixed.getUserSelectedElement(), fixedSource, "userSelected", focus, pattern);
  }

  private void checkCoding(List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition theElementCntext, boolean inCodeableConcept, boolean checkDisplay, NodeStack stack) {
    String code = element.getNamedChildValue("code");
    String system = element.getNamedChildValue("system");
    String display = element.getNamedChildValue("display");
    rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, isAbsolute(system), I18nConstants.TERMINOLOGY_TX_SYSTEM_RELATIVE);

    if (system != null && code != null && !noTerminologyChecks) {
      rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, !isValueSet(system), I18nConstants.TERMINOLOGY_TX_SYSTEM_VALUESET2, system);
      try {
        if (checkCode(errors, element, path, code, system, display, checkDisplay, stack))
          if (theElementCntext != null && theElementCntext.hasBinding()) {
            ElementDefinitionBindingComponent binding = theElementCntext.getBinding();
            if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, binding != null, I18nConstants.TERMINOLOGY_TX_BINDING_MISSING2, path)) {
              if (binding.hasValueSet()) {
                ValueSet valueset = resolveBindingReference(profile, binding.getValueSet(), profile.getUrl());
                if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, valueset != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(binding.getValueSet()))) {
                  try {
                    Coding c = ObjectConverter.readAsCoding(element);
                    long t = System.nanoTime();
                    ValidationResult vr = null;
                    if (binding.getStrength() != BindingStrength.EXAMPLE) {
                      vr = context.validateCode(new ValidationOptions(stack.workingLang), c, valueset);
                    }
                    txTime = txTime + (System.nanoTime() - t);
                    if (vr != null && !vr.isOk()) {
                      if (vr.IsNoService())
                        txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSERVER);
                      else if (vr.getErrorClass() != null && !vr.getErrorClass().isInfrastructure()) {
                        if (binding.getStrength() == BindingStrength.REQUIRED)
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_4, describeReference(binding.getValueSet(), valueset));
                        else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                          if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                            checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), c, stack);
                          else if (!noExtensibleWarnings)
                            txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_5, describeReference(binding.getValueSet(), valueset));
                        } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                          if (baseOnly) {
                            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_CONFIRM_6, describeReference(binding.getValueSet(), valueset));
                          }
                        }
                      } else if (binding.getStrength() == BindingStrength.REQUIRED)
                        txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_12, describeReference(binding.getValueSet(), valueset), getErrorMessage(vr.getMessage()));
                      else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
                        if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
                          checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), c, stack);
                        else
                          txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_13, describeReference(binding.getValueSet(), valueset), getErrorMessage(vr.getMessage()));
                      } else if (binding.getStrength() == BindingStrength.PREFERRED) {
                        if (baseOnly) {
                          txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_14, describeReference(binding.getValueSet(), valueset), getErrorMessage(vr.getMessage()));
                        }
                      }
                    }
                  } catch (Exception e) {
                    warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODING1, e.getMessage());
                  }
                }
              } else if (binding.hasValueSet()) {
                hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_CANTCHECK);
              } else if (!inCodeableConcept && !noBindingMsgSuppressed) {
                hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_BINDING_NOSOURCE, path);
              }
            }
          }
      } catch (Exception e) {
        rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_ERROR_CODING2, e.getMessage(), e.toString());
      }
    }
  }

  private boolean isValueSet(String url) {
    try {
      ValueSet vs = context.fetchResourceWithException(ValueSet.class, url);
      return vs != null;
    } catch (Exception e) {
      return false;
    }
  }

  private void checkContactPoint(List<ValidationMessage> errors, String path, Element focus, ContactPoint fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern);
    checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern);
    checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern);
    checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern);

  }

  private StructureDefinition checkExtension(ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, Element resource, Element container, Element element, ElementDefinition def, StructureDefinition profile, NodeStack stack, NodeStack containerStack, String extensionUrl) throws FHIRException {
    String url = element.getNamedChildValue("url");
    boolean isModifier = element.getName().equals("modifierExtension");

    long t = System.nanoTime();
    StructureDefinition ex = Utilities.isAbsoluteUrl(url) ? context.fetchResource(StructureDefinition.class, url) : null;
    sdTime = sdTime + (System.nanoTime() - t);
    if (ex == null) {
      if (xverManager == null) {
        xverManager = new XVerExtensionManager(context);
      }
      if (xverManager.matchingUrl(url)) {
        switch (xverManager.status(url)) {
          case BadVersion:
            rule(errors, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INVALID, url, xverManager.getVersion(url));
            break;
          case Unknown:
            rule(errors, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INVALIDID, url, xverManager.getElementId(url));
            break;
          case Invalid:
            rule(errors, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_NOCHANGE, url, xverManager.getElementId(url));
            break;
          case Valid:
            ex = xverManager.makeDefinition(url);
            context.generateSnapshot(ex);
            context.cacheResource(ex);
            break;
          default:
            rule(errors, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INTERNAL, url);
            break;
        }
      } else if (extensionUrl != null && !isAbsolute(url)) {
        if (extensionUrl.equals(profile.getUrl())) {
          rule(errors, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", hasExtensionSlice(profile, url), I18nConstants.EXTENSION_EXT_SUBEXTENSION_INVALID, url, profile.getUrl());
        }
      } else if (rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, allowUnknownExtension(url), I18nConstants.EXTENSION_EXT_UNKNOWN_NOTHERE, url)) {
        hint(errors, IssueType.STRUCTURE, element.line(), element.col(), path, isKnownExtension(url), I18nConstants.EXTENSION_EXT_UNKNOWN, url);
      }
    }
    if (ex != null) {
      trackUsage(ex, hostContext, element);
      if (def.getIsModifier()) {
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path + "[url='" + url + "']", ex.getSnapshot().getElement().get(0).getIsModifier(), I18nConstants.EXTENSION_EXT_MODIFIER_MISMATCHY);
      } else {
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path + "[url='" + url + "']", !ex.getSnapshot().getElement().get(0).getIsModifier(), I18nConstants.EXTENSION_EXT_MODIFIER_MISMATCHN);
      }
      // two questions
      // 1. can this extension be used here?
      checkExtensionContext(errors, resource, container, ex, containerStack, hostContext);

      if (isModifier)
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path + "[url='" + url + "']", ex.getSnapshot().getElement().get(0).getIsModifier(), I18nConstants.EXTENSION_EXT_MODIFIER_Y, url);
      else
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path + "[url='" + url + "']", !ex.getSnapshot().getElement().get(0).getIsModifier(), I18nConstants.EXTENSION_EXT_MODIFIER_N, url);

      // check the type of the extension:
      Set<String> allowedTypes = listExtensionTypes(ex);
      String actualType = getExtensionType(element);
      if (actualType == null)
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, allowedTypes.isEmpty(), I18nConstants.EXTENSION_EXT_SIMPLE, url);
      else
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, allowedTypes.contains(actualType), I18nConstants.EXTENSION_EXT_TYPE, url, allowedTypes.toString(), actualType);

      // 3. is the content of the extension valid?
      validateElement(hostContext, errors, ex, ex.getSnapshot().getElement().get(0), null, null, resource, element, "Extension", stack, false, true, url);

    }
    return ex;
  }

  private boolean hasExtensionSlice(StructureDefinition profile, String sliceName) {
    for (ElementDefinition ed : profile.getSnapshot().getElement()) {
      if (ed.getPath().equals("Extension.extension.url") && ed.hasFixed() && sliceName.equals(ed.getFixed().primitiveValue())) {
        return true;
      }
    }
    return false;
  }

  private String getExtensionType(Element element) {
    for (Element e : element.getChildren()) {
      if (e.getName().startsWith("value")) {
        String tn = e.getName().substring(5);
        String ltn = Utilities.uncapitalize(tn);
        if (isPrimitiveType(ltn))
          return ltn;
        else
          return tn;
      }
    }
    return null;
  }

  private Set<String> listExtensionTypes(StructureDefinition ex) {
    ElementDefinition vd = null;
    for (ElementDefinition ed : ex.getSnapshot().getElement()) {
      if (ed.getPath().startsWith("Extension.value")) {
        vd = ed;
        break;
      }
    }
    Set<String> res = new HashSet<String>();
    if (vd != null && !"0".equals(vd.getMax())) {
      for (TypeRefComponent tr : vd.getType()) {
        res.add(tr.getWorkingCode());
      }
    }
    return res;
  }

  private boolean checkExtensionContext(List<ValidationMessage> errors, Element resource, Element container, StructureDefinition definition, NodeStack stack, ValidatorHostContext hostContext) {
    String extUrl = definition.getUrl();
    boolean ok = false;
    CommaSeparatedStringBuilder contexts = new CommaSeparatedStringBuilder();
    List<String> plist = new ArrayList<>();
    plist.add(stripIndexes(stack.getLiteralPath()));
    for (String s : stack.getLogicalPaths()) {
      String p = stripIndexes(s);
      // all extensions are always allowed in ElementDefinition.example.value, and in fixed and pattern values. TODO: determine the logical paths from the path stated in the element definition....
      if (Utilities.existsInList(p, "ElementDefinition.example.value", "ElementDefinition.pattern", "ElementDefinition.fixed")) {
        return true;
      }
      plist.add(p);

    }

    for (StructureDefinitionContextComponent ctxt : fixContexts(extUrl, definition.getContext())) {
      if (ok) {
        break;
      }
      if (ctxt.getType() == ExtensionContextType.ELEMENT) {
        String en = ctxt.getExpression();
        contexts.append("e:" + en);
        if ("Element".equals(en)) {
          ok = true;
        } else if (en.equals("Resource") && container.isResource()) {
          ok = true;
        }
        for (String p : plist) {
          if (ok) {
            break;
          }
          if (p.equals(en)) {
            ok = true;
          } else {
            String pn = p;
            String pt = "";
            if (p.contains(".")) {
              pn = p.substring(0, p.indexOf("."));
              pt = p.substring(p.indexOf("."));
            }
            StructureDefinition sd = context.fetchTypeDefinition(pn);
            while (sd != null) {
              if ((sd.getType() + pt).equals(en)) {
                ok = true;
                break;
              }
              if (sd.getBaseDefinition() != null) {
                sd = context.fetchResource(StructureDefinition.class, sd.getBaseDefinition());
              } else {
                sd = null;
              }
            }
          }
        }
      } else if (ctxt.getType() == ExtensionContextType.EXTENSION) {
        contexts.append("x:" + ctxt.getExpression());
        NodeStack estack = stack.parent;
        if (estack != null && estack.getElement().fhirType().equals("Extension")) {
          String ext = estack.element.getNamedChildValue("url");
          if (ctxt.getExpression().equals(ext)) {
            ok = true;
          }
        }
      } else if (ctxt.getType() == ExtensionContextType.FHIRPATH) {
        contexts.append("p:" + ctxt.getExpression());
        // The context is all elements that match the FHIRPath query found in the expression.
        List<Base> res = fpe.evaluate(hostContext, resource, hostContext.getRootResource(), container, fpe.parse(ctxt.getExpression()));
        if (res.contains(container)) {
          ok = true;
        }
      } else {
        throw new Error(context.formatMessage(I18nConstants.UNRECOGNISED_EXTENSION_CONTEXT_, ctxt.getTypeElement().asStringValue()));
      }
    }
    if (!ok) {
      rule(errors, IssueType.STRUCTURE, container.line(), container.col(), stack.literalPath, false, I18nConstants.EXTENSION_EXT_CONTEXT_WRONG, extUrl, contexts.toString(), plist.toString());
      return false;
    } else {
      if (definition.hasContextInvariant()) {
        for (StringType s : definition.getContextInvariant()) {
          if (!fpe.evaluateToBoolean(hostContext, resource, hostContext.getRootResource(), container, fpe.parse(s.getValue()))) {
            rule(errors, IssueType.STRUCTURE, container.line(), container.col(), stack.literalPath, false, I18nConstants.PROFILE_EXT_NOT_HERE, extUrl, s.getValue());
            return false;
          }
        }
      }
      return true;
    }
  }

  private List<StructureDefinitionContextComponent> fixContexts(String extUrl, List<StructureDefinitionContextComponent> list) {
    List<StructureDefinitionContextComponent> res = new ArrayList<>();
    for (StructureDefinitionContextComponent ctxt : list) {
      res.add(ctxt.copy());
    }
    if ("http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type".equals(extUrl)) {
      list.get(0).setExpression("ElementDefinition.type");
    }
    if ("http://hl7.org/fhir/StructureDefinition/regex".equals(extUrl)) {
      list.get(1).setExpression("ElementDefinition.type");
    }
    return list;
  }

  private String stripIndexes(String path) {
    boolean skip = false;
    StringBuilder b = new StringBuilder();
    for (char c : path.toCharArray()) {
      if (skip) {
        if (c == ']') {
          skip = false;
        }
      } else if (c == '[') {
        skip = true;
      } else {
        b.append(c);
      }
    }
    return b.toString();
  }

  private void checkFixedValue(List<ValidationMessage> errors, String path, Element focus, org.hl7.fhir.r5.model.Element fixed, String fixedSource, String propName, Element parent) {
    checkFixedValue(errors, path, focus, fixed, fixedSource, propName, parent, false);
  }

  @SuppressWarnings("rawtypes")
  private void checkFixedValue(List<ValidationMessage> errors, String path, Element focus, org.hl7.fhir.r5.model.Element fixed, String fixedSource, String propName, Element parent, boolean pattern) {
    if ((fixed == null || fixed.isEmpty()) && focus == null) {
      ; // this is all good
    } else if ((fixed == null || fixed.isEmpty()) && focus != null) {
      rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, pattern, I18nConstants.PROFILE_VAL_NOTALLOWED, focus.getName(), (pattern ? "pattern" : "fixed value"));
    } else if (fixed != null && !fixed.isEmpty() && focus == null) {
      rule(errors, IssueType.VALUE, parent == null ? -1 : parent.line(), parent == null ? -1 : parent.col(), path, false, I18nConstants.PROFILE_VAL_MISSINGELEMENT, propName, fixedSource);
    } else {
      String value = focus.primitiveValue();
      if (fixed instanceof org.hl7.fhir.r5.model.BooleanType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.BooleanType) fixed).asStringValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.BooleanType) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.IntegerType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.IntegerType) fixed).asStringValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.IntegerType) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.DecimalType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.DecimalType) fixed).asStringValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.DecimalType) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.Base64BinaryType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.Base64BinaryType) fixed).asStringValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.Base64BinaryType) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.InstantType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.InstantType) fixed).getValue().toString(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.InstantType) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.CodeType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.CodeType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.CodeType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.Enumeration)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.Enumeration) fixed).asStringValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.Enumeration) fixed).asStringValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.StringType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.StringType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.StringType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.UriType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.UriType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.UriType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.DateType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.DateType) fixed).getValue().toString(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.DateType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.DateTimeType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.DateTimeType) fixed).getValue().toString(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.DateTimeType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.OidType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.OidType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.OidType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.UuidType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.UuidType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.UuidType) fixed).getValue());
      else if (fixed instanceof org.hl7.fhir.r5.model.IdType)
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, check(((org.hl7.fhir.r5.model.IdType) fixed).getValue(), value), I18nConstants._DT_FIXED_WRONG, value, ((org.hl7.fhir.r5.model.IdType) fixed).getValue());
      else if (fixed instanceof Quantity)
        checkQuantity(errors, path, focus, (Quantity) fixed, fixedSource, pattern);
      else if (fixed instanceof Address)
        checkAddress(errors, path, focus, (Address) fixed, fixedSource, pattern);
      else if (fixed instanceof ContactPoint)
        checkContactPoint(errors, path, focus, (ContactPoint) fixed, fixedSource, pattern);
      else if (fixed instanceof Attachment)
        checkAttachment(errors, path, focus, (Attachment) fixed, fixedSource, pattern);
      else if (fixed instanceof Identifier)
        checkIdentifier(errors, path, focus, (Identifier) fixed, fixedSource, pattern);
      else if (fixed instanceof Coding)
        checkCoding(errors, path, focus, (Coding) fixed, fixedSource, pattern);
      else if (fixed instanceof HumanName)
        checkHumanName(errors, path, focus, (HumanName) fixed, fixedSource, pattern);
      else if (fixed instanceof CodeableConcept)
        checkCodeableConcept(errors, path, focus, (CodeableConcept) fixed, fixedSource, pattern);
      else if (fixed instanceof Timing)
        checkTiming(errors, path, focus, (Timing) fixed, fixedSource, pattern);
      else if (fixed instanceof Period)
        checkPeriod(errors, path, focus, (Period) fixed, fixedSource, pattern);
      else if (fixed instanceof Range)
        checkRange(errors, path, focus, (Range) fixed, fixedSource, pattern);
      else if (fixed instanceof Ratio)
        checkRatio(errors, path, focus, (Ratio) fixed, fixedSource, pattern);
      else if (fixed instanceof SampledData)
        checkSampledData(errors, path, focus, (SampledData) fixed, fixedSource, pattern);

      else
        rule(errors, IssueType.EXCEPTION, focus.line(), focus.col(), path, false, I18nConstants.INTERNAL_INT_BAD_TYPE, fixed.getClass().getName());
      List<Element> extensions = new ArrayList<Element>();
      focus.getNamedChildren("extension", extensions);
      if (fixed.getExtension().size() == 0) {
        rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, extensions.size() == 0, I18nConstants.EXTENSION_EXT_FIXED_BANNED);
      } else if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, extensions.size() == fixed.getExtension().size(), I18nConstants.EXTENSION_EXT_COUNT_MISMATCH, Integer.toString(fixed.getExtension().size()), Integer.toString(extensions.size()))) {
        for (Extension e : fixed.getExtension()) {
          Element ex = getExtensionByUrl(extensions, e.getUrl());
          if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, ex != null, I18nConstants.EXTENSION_EXT_COUNT_NOTFOUND, e.getUrl())) {
            checkFixedValue(errors, path, ex.getNamedChild("extension").getNamedChild("value"), e.getValue(), fixedSource, "extension.value", ex.getNamedChild("extension"));
          }
        }
      }
    }
  }

  private void checkHumanName(List<ValidationMessage> errors, String path, Element focus, HumanName fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern);
    checkFixedValue(errors, path + ".text", focus.getNamedChild("text"), fixed.getTextElement(), fixedSource, "text", focus, pattern);
    checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern);

    List<Element> parts = new ArrayList<Element>();
    focus.getNamedChildren("family", parts);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, parts.size() > 0 == fixed.hasFamily(), I18nConstants.FIXED_TYPE_CHECKS_DT_NAME_FAMILY, (fixed.hasFamily() ? "1" : "0"), Integer.toString(parts.size()))) {
      for (int i = 0; i < parts.size(); i++)
        checkFixedValue(errors, path + ".family", parts.get(i), fixed.getFamilyElement(), fixedSource, "family", focus, pattern);
    }
    focus.getNamedChildren("given", parts);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, parts.size() == fixed.getGiven().size(), I18nConstants.FIXED_TYPE_CHECKS_DT_NAME_GIVEN, Integer.toString(fixed.getGiven().size()), Integer.toString(parts.size()))) {
      for (int i = 0; i < parts.size(); i++)
        checkFixedValue(errors, path + ".given", parts.get(i), fixed.getGiven().get(i), fixedSource, "given", focus, pattern);
    }
    focus.getNamedChildren("prefix", parts);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, parts.size() == fixed.getPrefix().size(), I18nConstants.FIXED_TYPE_CHECKS_DT_NAME_PREFIX, Integer.toString(fixed.getPrefix().size()), Integer.toString(parts.size()))) {
      for (int i = 0; i < parts.size(); i++)
        checkFixedValue(errors, path + ".prefix", parts.get(i), fixed.getPrefix().get(i), fixedSource, "prefix", focus, pattern);
    }
    focus.getNamedChildren("suffix", parts);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, parts.size() == fixed.getSuffix().size(), I18nConstants.FIXED_TYPE_CHECKS_DT_NAME_SUFFIX, Integer.toString(fixed.getSuffix().size()), Integer.toString(parts.size()))) {
      for (int i = 0; i < parts.size(); i++)
        checkFixedValue(errors, path + ".suffix", parts.get(i), fixed.getSuffix().get(i), fixedSource, "suffix", focus, pattern);
    }
  }

  private void checkIdentifier(List<ValidationMessage> errors, String path, Element element, ElementDefinition context) {
    String system = element.getNamedChildValue("system");
    rule(errors, IssueType.CODEINVALID, element.line(), element.col(), path, isAbsolute(system), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_IDENTIFIER_SYSTEM);
  }

  private void checkIdentifier(List<ValidationMessage> errors, String path, Element focus, Identifier fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".use", focus.getNamedChild("use"), fixed.getUseElement(), fixedSource, "use", focus, pattern);
    checkFixedValue(errors, path + ".type", focus.getNamedChild("type"), fixed.getType(), fixedSource, "type", focus, pattern);
    checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern);
    checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern);
    checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriod(), fixedSource, "period", focus, pattern);
    checkFixedValue(errors, path + ".assigner", focus.getNamedChild("assigner"), fixed.getAssigner(), fixedSource, "assigner", focus, pattern);
  }

  private void checkPeriod(List<ValidationMessage> errors, String path, Element focus, Period fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".start", focus.getNamedChild("start"), fixed.getStartElement(), fixedSource, "start", focus, pattern);
    checkFixedValue(errors, path + ".end", focus.getNamedChild("end"), fixed.getEndElement(), fixedSource, "end", focus, pattern);
  }

  private void checkPrimitive(Object appContext, List<ValidationMessage> errors, String path, String type, ElementDefinition context, Element e, StructureDefinition profile, NodeStack node) throws FHIRException {
    if (isBlank(e.primitiveValue())) {
      if (e.primitiveValue() == null)
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_VALUEEXT);
      else if (e.primitiveValue().length() == 0)
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_NOTEMPTY);
      else if (StringUtils.isWhitespace(e.primitiveValue()))
        warning(errors, IssueType.INVALID, e.line(), e.col(), path, e.hasChildren(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_WS);
      return;
    }
    String regex = context.getExtensionString(ToolingExtensions.EXT_REGEX);
    if (regex != null)
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.primitiveValue().matches(regex), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_REGEX, e.primitiveValue(), regex);

    if (type.equals("boolean")) {
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, "true".equals(e.primitiveValue()) || "false".equals(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_BOOLEAN_VALUE);
    }
    if (type.equals("uri") || type.equals("oid") || type.equals("uuid") || type.equals("url") || type.equals("canonical")) {
      String url = e.primitiveValue();
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !url.startsWith("oid:"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_URI_OID);
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !url.startsWith("uuid:"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_URI_UUID);
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, url.equals(url.trim().replace(" ", ""))
        // work around an old invalid example in a core package
        || "http://www.acme.com/identifiers/patient or urn:ietf:rfc:3986 if the Identifier.value itself is a full uri".equals(url), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_URI_WS, url);
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || url.length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());

      if (type.equals("oid")) {
        if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, url.startsWith("urn:oid:"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_OID_START))
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, Utilities.isOid(url.substring(8)), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_OID_VALID);
      }
      if (type.equals("uuid")) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, url.startsWith("urn:uuid:"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_UUID_STRAT);
        try {
          UUID.fromString(url.substring(8));
        } catch (Exception ex) {
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_UUID_VAID, ex.getMessage());
        }
      }

      // now, do we check the URI target?
      if (fetcher != null) {
        boolean found;
        try {
          found = isDefinitionURL(url) || (allowExamples && (url.contains("example.org") || url.contains("acme.com"))) || (url.startsWith("http://hl7.org/fhir/tools")) || fetcher.resolveURL(appContext, path, url);
        } catch (IOException e1) {
          found = false;
        }
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, found, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_URL_RESOLVE, url);
      }
    }
    if (type.equals("id")) {
      // work around an old issue with ElementDefinition.id
      if (!context.getPath().equals("ElementDefinition.id") && !VersionUtilities.versionsCompatible("1.4", this.context.getVersion())) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, FormatUtilities.isValidId(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_ID_VALID, e.primitiveValue());
      }
    }
    if (type.equalsIgnoreCase("string") && e.hasPrimitiveValue()) {
      if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.primitiveValue() == null || e.primitiveValue().length() > 0, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_NOTEMPTY)) {
        warning(errors, IssueType.INVALID, e.line(), e.col(), path, e.primitiveValue() == null || e.primitiveValue().trim().equals(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_STRING_WS);
        if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.primitiveValue().length() <= 1048576, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_STRING_LENGTH)) {
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());
        }
      }
    }
    if (type.equals("dateTime")) {
      warning(errors, IssueType.INVALID, e.line(), e.col(), path, yearIsValid(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_REASONABLE, e.primitiveValue());
      rule(errors, IssueType.INVALID, e.line(), e.col(), path,
        e.primitiveValue()
          .matches("([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?)?)?)?"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_VALID, e.primitiveValue());
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !hasTime(e.primitiveValue()) || hasTimeZone(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_TZ);
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());
      try {
        DateTimeType dt = new DateTimeType(e.primitiveValue());
      } catch (Exception ex) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_VALID, ex.getMessage());
      }
    }
    if (type.equals("time")) {
      rule(errors, IssueType.INVALID, e.line(), e.col(), path,
        e.primitiveValue()
          .matches("([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_TIME_VALID);
      try {
        TimeType dt = new TimeType(e.primitiveValue());
      } catch (Exception ex) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_TIME_VALID, ex.getMessage());
      }
    }
    if (type.equals("date")) {
      warning(errors, IssueType.INVALID, e.line(), e.col(), path, yearIsValid(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_REASONABLE, e.primitiveValue());
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, e.primitiveValue().matches("([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATE_VALID);
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());
      try {
        DateType dt = new DateType(e.primitiveValue());
      } catch (Exception ex) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATE_VALID, ex.getMessage());
      }
    }
    if (type.equals("base64Binary")) {
      String encoded = e.primitiveValue();
      if (isNotBlank(encoded)) {
        /*
         * Technically this is not bulletproof as some invalid base64 won't be caught,
         * but I think it's good enough. The original code used Java8 Base64 decoder
         * but I've replaced it with a regex for 2 reasons:
         * 1. This code will run on any version of Java
         * 2. This code doesn't actually decode, which is much easier on memory use for big payloads
         */
        int charCount = 0;
        for (int i = 0; i < encoded.length(); i++) {
          char nextChar = encoded.charAt(i);
          if (Character.isWhitespace(nextChar)) {
            continue;
          }
          if (Character.isLetterOrDigit(nextChar)) {
            charCount++;
          }
          if (nextChar == '/' || nextChar == '=' || nextChar == '+') {
            charCount++;
          }
        }

        if (charCount > 0 && charCount % 4 != 0) {
          String value = encoded.length() < 100 ? encoded : "(snip)";
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_BASE64_VALID, value);
        }
      }
    }
    if (type.equals("integer") || type.equals("unsignedInt") || type.equals("positiveInt")) {
      if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, Utilities.isInteger(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_VALID, e.primitiveValue())) {
        Integer v = new Integer(e.getValue()).intValue();
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxValueIntegerType() || !context.getMaxValueIntegerType().hasValue() || (context.getMaxValueIntegerType().getValue() >= v), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_GT, (context.hasMaxValueIntegerType() ? context.getMaxValueIntegerType() : ""));
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMinValueIntegerType() || !context.getMinValueIntegerType().hasValue() || (context.getMinValueIntegerType().getValue() <= v), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT, (context.hasMinValueIntegerType() ? context.getMinValueIntegerType() : ""));
        if (type.equals("unsignedInt"))
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, v >= 0, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT0);
        if (type.equals("positiveInt"))
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, v > 0, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT1);
      }
    }
    if (type.equals("integer64")) {
      if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, Utilities.isLong(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER64_VALID, e.primitiveValue())) {
        Long v = new Long(e.getValue()).longValue();
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxValueInteger64Type() || !context.getMaxValueInteger64Type().hasValue() || (context.getMaxValueInteger64Type().getValue() >= v), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_GT, (context.hasMaxValueInteger64Type() ? context.getMaxValueInteger64Type() : ""));
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMinValueInteger64Type() || !context.getMinValueInteger64Type().hasValue() || (context.getMinValueInteger64Type().getValue() <= v), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT, (context.hasMinValueInteger64Type() ? context.getMinValueInteger64Type() : ""));
        if (type.equals("unsignedInt"))
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, v >= 0, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT0);
        if (type.equals("positiveInt"))
          rule(errors, IssueType.INVALID, e.line(), e.col(), path, v > 0, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INTEGER_LT1);
      }
    }
    if (type.equals("decimal")) {
      if (e.primitiveValue() != null) {
        DecimalStatus ds = Utilities.checkDecimal(e.primitiveValue(), true, false);
        if (rule(errors, IssueType.INVALID, e.line(), e.col(), path, ds == DecimalStatus.OK || ds == DecimalStatus.RANGE, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DECIMAL_VALID, e.primitiveValue()))
          warning(errors, IssueType.VALUE, e.line(), e.col(), path, ds != DecimalStatus.RANGE, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DECIMAL_RANGE, e.primitiveValue());
      }
    }
    if (type.equals("instant")) {
      rule(errors, IssueType.INVALID, e.line(), e.col(), path,
        e.primitiveValue().matches("-?[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))"), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_REGEX, e.primitiveValue());
      warning(errors, IssueType.INVALID, e.line(), e.col(), path, yearIsValid(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_DATETIME_REASONABLE, e.primitiveValue());
      try {
        InstantType dt = new InstantType(e.primitiveValue());
      } catch (Exception ex) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.TYPE_SPECIFIC_CHECKS_DT_INSTANT_VALID, ex.getMessage());
      }
    }

    if (type.equals("code") && e.primitiveValue() != null) {
      // Technically, a code is restricted to string which has at least one character and no leading or trailing whitespace, and where there is no whitespace
      // other than single spaces in the contents
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, passesCodeWhitespaceRules(e.primitiveValue()), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_CODE_WS, e.primitiveValue());
      rule(errors, IssueType.INVALID, e.line(), e.col(), path, !context.hasMaxLength() || context.getMaxLength() == 0 || e.primitiveValue().length() <= context.getMaxLength(), I18nConstants.TYPE_SPECIFIC_CHECKS_DT_PRIMITIVE_LENGTH, context.getMaxLength());
    }

    if (context.hasBinding() && e.primitiveValue() != null) {
      checkPrimitiveBinding(errors, path, type, context, e, profile, node);
    }

    if (type.equals("xhtml")) {
      XhtmlNode xhtml = e.getXhtml();
      if (xhtml != null) { // if it is null, this is an error already noted in the parsers
        // check that the namespace is there and correct.
        String ns = xhtml.getNsDecl();
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, FormatUtilities.XHTML_NS.equals(ns), I18nConstants.XHTML_XHTML_NS_INVALID, ns, FormatUtilities.XHTML_NS);
        // check that inner namespaces are all correct
        checkInnerNS(errors, e, path, xhtml.getChildNodes());
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, "div".equals(xhtml.getName()), I18nConstants.XHTML_XHTML_NAME_INVALID, ns);
        // check that no illegal elements and attributes have been used
        checkInnerNames(errors, e, path, xhtml.getChildNodes());
      }
    }

    if (context.hasFixed()) {
      checkFixedValue(errors, path, e, context.getFixed(), profile.getUrl(), context.getSliceName(), null, false);
    }
    if (context.hasPattern()) {
      checkFixedValue(errors, path, e, context.getPattern(), profile.getUrl(), context.getSliceName(), null, true);
    }

    // for nothing to check
  }

  private boolean isDefinitionURL(String url) {
    return Utilities.existsInList(url, "http://hl7.org/fhirpath/System.Boolean", "http://hl7.org/fhirpath/System.String", "http://hl7.org/fhirpath/System.Integer",
      "http://hl7.org/fhirpath/System.Decimal", "http://hl7.org/fhirpath/System.Date", "http://hl7.org/fhirpath/System.Time", "http://hl7.org/fhirpath/System.DateTime", "http://hl7.org/fhirpath/System.Quantity");
  }

  private void checkInnerNames(List<ValidationMessage> errors, Element e, String path, List<XhtmlNode> list) {
    for (XhtmlNode node : list) {
      if (node.getNodeType() == NodeType.Element) {
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, Utilities.existsInList(node.getName(),
          "p", "br", "div", "h1", "h2", "h3", "h4", "h5", "h6", "a", "span", "b", "em", "i", "strong",
          "small", "big", "tt", "small", "dfn", "q", "var", "abbr", "acronym", "cite", "blockquote", "hr", "address", "bdo", "kbd", "q", "sub", "sup",
          "ul", "ol", "li", "dl", "dt", "dd", "pre", "table", "caption", "colgroup", "col", "thead", "tr", "tfoot", "tbody", "th", "td",
          "code", "samp", "img", "map", "area"

        ), I18nConstants.XHTML_XHTML_ELEMENT_ILLEGAL, node.getName());
        for (String an : node.getAttributes().keySet()) {
          boolean ok = an.startsWith("xmlns") || Utilities.existsInList(an,
            "title", "style", "class", "id", "lang", "xml:lang", "dir", "accesskey", "tabindex",
            // tables
            "span", "width", "align", "valign", "char", "charoff", "abbr", "axis", "headers", "scope", "rowspan", "colspan") ||

            Utilities.existsInList(node.getName() + "." + an, "a.href", "a.name", "img.src", "img.border", "div.xmlns", "blockquote.cite", "q.cite",
              "a.charset", "a.type", "a.name", "a.href", "a.hreflang", "a.rel", "a.rev", "a.shape", "a.coords", "img.src",
              "img.alt", "img.longdesc", "img.height", "img.width", "img.usemap", "img.ismap", "map.name", "area.shape",
              "area.coords", "area.href", "area.nohref", "area.alt", "table.summary", "table.width", "table.border",
              "table.frame", "table.rules", "table.cellspacing", "table.cellpadding", "pre.space", "td.nowrap"
            );
          if (!ok)
            rule(errors, IssueType.INVALID, e.line(), e.col(), path, false, I18nConstants.XHTML_XHTML_ATTRIBUTE_ILLEGAL, an, node.getName());
        }
        checkInnerNames(errors, e, path, node.getChildNodes());
      }
    }
  }

  private void checkInnerNS(List<ValidationMessage> errors, Element e, String path, List<XhtmlNode> list) {
    for (XhtmlNode node : list) {
      if (node.getNodeType() == NodeType.Element) {
        String ns = node.getNsDecl();
        rule(errors, IssueType.INVALID, e.line(), e.col(), path, ns == null || FormatUtilities.XHTML_NS.equals(ns), I18nConstants.XHTML_XHTML_NS_INVALID, ns, FormatUtilities.XHTML_NS);
        checkInnerNS(errors, e, path, node.getChildNodes());
      }
    }
  }

  private void checkPrimitiveBinding(List<ValidationMessage> errors, String path, String type, ElementDefinition elementContext, Element element, StructureDefinition profile, NodeStack stack) {
    // We ignore bindings that aren't on string, uri or code
    if (!element.hasPrimitiveValue() || !("code".equals(type) || "string".equals(type) || "uri".equals(type) || "url".equals(type) || "canonical".equals(type))) {
      return;
    }
    if (noTerminologyChecks)
      return;

    String value = element.primitiveValue();
    // System.out.println("check "+value+" in "+path);

    // firstly, resolve the value set
    ElementDefinitionBindingComponent binding = elementContext.getBinding();
    if (binding.hasValueSet()) {
      ValueSet vs = resolveBindingReference(profile, binding.getValueSet(), profile.getUrl());
      if (warning(errors, IssueType.CODEINVALID, element.line(), element.col(), path, vs != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND2, describeReference(binding.getValueSet()))) {
        long t = System.nanoTime();
        ValidationResult vr = null;
        if (binding.getStrength() != BindingStrength.EXAMPLE) {
          ValidationOptions options = new ValidationOptions(stack.workingLang).guessSystem();
          vr = context.validateCode(options, value, vs);
        }
        txTime = txTime + (System.nanoTime() - t);
        if (vr != null && !vr.isOk()) {
          if (vr.IsNoService())
            txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_15, value);
          else if (binding.getStrength() == BindingStrength.REQUIRED)
            txRule(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_16, value, describeReference(binding.getValueSet()), vs.getUrl(), getErrorMessage(vr.getMessage()));
          else if (binding.getStrength() == BindingStrength.EXTENSIBLE) {
            if (binding.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"))
              checkMaxValueSet(errors, path, element, profile, ToolingExtensions.readStringExtension(binding, "http://hl7.org/fhir/StructureDefinition/elementdefinition-maxValueSet"), value, stack);
            else if (!noExtensibleWarnings)
              txWarning(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_17, value, describeReference(binding.getValueSet()), vs.getUrl(), getErrorMessage(vr.getMessage()));
          } else if (binding.getStrength() == BindingStrength.PREFERRED) {
            if (baseOnly) {
              txHint(errors, vr.getTxLink(), IssueType.CODEINVALID, element.line(), element.col(), path, false, I18nConstants.TERMINOLOGY_TX_NOVALID_18, value, describeReference(binding.getValueSet()), vs.getUrl(), getErrorMessage(vr.getMessage()));
            }
          }
        }
      }
    } else if (!noBindingMsgSuppressed)
      hint(errors, IssueType.CODEINVALID, element.line(), element.col(), path, !type.equals("code"), I18nConstants.TERMINOLOGY_TX_BINDING_NOSOURCE2);
  }

  private void checkQuantity(List<ValidationMessage> errors, String path, Element focus, Quantity fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".value", focus.getNamedChild("value"), fixed.getValueElement(), fixedSource, "value", focus, pattern);
    checkFixedValue(errors, path + ".comparator", focus.getNamedChild("comparator"), fixed.getComparatorElement(), fixedSource, "comparator", focus, pattern);
    checkFixedValue(errors, path + ".units", focus.getNamedChild("unit"), fixed.getUnitElement(), fixedSource, "units", focus, pattern);
    checkFixedValue(errors, path + ".system", focus.getNamedChild("system"), fixed.getSystemElement(), fixedSource, "system", focus, pattern);
    checkFixedValue(errors, path + ".code", focus.getNamedChild("code"), fixed.getCodeElement(), fixedSource, "code", focus, pattern);
  }

  // implementation

  private void checkRange(List<ValidationMessage> errors, String path, Element focus, Range fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".low", focus.getNamedChild("low"), fixed.getLow(), fixedSource, "low", focus, pattern);
    checkFixedValue(errors, path + ".high", focus.getNamedChild("high"), fixed.getHigh(), fixedSource, "high", focus, pattern);

  }

  private void checkRatio(List<ValidationMessage> errors, String path, Element focus, Ratio fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".numerator", focus.getNamedChild("numerator"), fixed.getNumerator(), fixedSource, "numerator", focus, pattern);
    checkFixedValue(errors, path + ".denominator", focus.getNamedChild("denominator"), fixed.getDenominator(), fixedSource, "denominator", focus, pattern);
  }

  private void checkReference(ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, Element element, StructureDefinition profile, ElementDefinition container, String parentType, NodeStack stack) throws FHIRException {
    Reference reference = ObjectConverter.readAsReference(element);

    String ref = reference.getReference();
    if (Utilities.noString(ref)) {
      if (Utilities.noString(reference.getIdentifier().getSystem()) && Utilities.noString(reference.getIdentifier().getValue())) {
        warning(errors, IssueType.STRUCTURE, element.line(), element.col(), path, !Utilities.noString(element.getNamedChildValue("display")), I18nConstants.REFERENCE_REF_NODISPLAY);
      }
      return;
    } else if (Utilities.existsInList(ref, "http://tools.ietf.org/html/bcp47")) {
      // special known URLs that can't be validated but are known to be valid
      return;
    }

    ResolvedReference we = localResolve(ref, stack, errors, path, (Element) hostContext.getAppContext(), element);
    String refType;
    if (ref.startsWith("#")) {
      refType = "contained";
    } else {
      if (we == null) {
        refType = "remote";
      } else {
        refType = "bundled";
      }
    }
    ReferenceValidationPolicy pol = refType.equals("contained") || refType.equals("bundled") ? ReferenceValidationPolicy.CHECK_VALID : fetcher == null ? ReferenceValidationPolicy.IGNORE : fetcher.validationPolicy(hostContext.getAppContext(), path, ref);

    if (pol.checkExists()) {
      if (we == null) {
        if (fetcher == null) {
          if (!refType.equals("contained"))
            throw new FHIRException(context.formatMessage(I18nConstants.RESOURCE_RESOLUTION_SERVICES_NOT_PROVIDED));
        } else {
          Element ext = null;
          if (fetchCache.containsKey(ref)) {
            ext = fetchCache.get(ref);
          } else {
            try {
              ext = fetcher.fetch(hostContext.getAppContext(), ref);
            } catch (IOException e) {
              throw new FHIRException(e);
            }
            if (ext != null) {
              fetchCache.put(ref, ext);
            }
          }
          we = ext == null ? null : makeExternalRef(ext, path);
        }
      }
      rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, (allowExamples && (ref.contains("example.org") || ref.contains("acme.com"))) || (we != null || pol == ReferenceValidationPolicy.CHECK_TYPE_IF_EXISTS), I18nConstants.REFERENCE_REF_CANTRESOLVE, ref);
    }

    String ft;
    if (we != null)
      ft = we.getType();
    else
      ft = tryParse(ref);

    if (reference.hasType()) { // R4 onwards...
      // the type has to match the specified
      String tu = isAbsolute(reference.getType()) ? reference.getType() : "http://hl7.org/fhir/StructureDefinition/" + reference.getType();
      TypeRefComponent containerType = container.getType("Reference");
      if (!containerType.hasTargetProfile(tu) && !containerType.hasTargetProfile("http://hl7.org/fhir/StructureDefinition/Resource")) {
        boolean matchingResource = false;
        for (CanonicalType target : containerType.getTargetProfile()) {
          StructureDefinition sd = resolveProfile(profile, target.asStringValue());
          if (("http://hl7.org/fhir/StructureDefinition/" + sd.getType()).equals(tu)) {
            matchingResource = true;
            break;
          }
        }
        rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, matchingResource, I18nConstants.REFERENCE_REF_WRONGTARGET, reference.getType(), container.getType("Reference").getTargetProfile());

      }
      // the type has to match the actual
      rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, ft == null || ft.equals(reference.getType()), I18nConstants.REFERENCE_REF_BADTARGETTYPE, reference.getType(), ft);
    }

    if (we != null && pol.checkType()) {
      if (warning(errors, IssueType.STRUCTURE, element.line(), element.col(), path, ft != null, I18nConstants.REFERENCE_REF_NOTYPE)) {
        // we validate as much as we can. First, can we infer a type from the profile?
        boolean ok = false;
        TypeRefComponent type = getReferenceTypeRef(container.getType());
        if (type.hasTargetProfile() && !type.hasTargetProfile("http://hl7.org/fhir/StructureDefinition/Resource")) {
          Set<String> types = new HashSet<>();
          List<StructureDefinition> profiles = new ArrayList<>();
          for (UriType u : type.getTargetProfile()) {
            StructureDefinition sd = resolveProfile(profile, u.getValue());
            if (rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, sd != null, I18nConstants.REFERENCE_REF_CANTRESOLVEPROFILE, u.getValue())) {
              types.add(sd.getType());
              if (ft.equals(sd.getType())) {
                ok = true;
                profiles.add(sd);
              }
            }
          }
          if (!pol.checkValid()) {
            rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, profiles.size() > 0, I18nConstants.REFERENCE_REF_CANTMATCHTYPE, ref, StringUtils.join("; ", type.getTargetProfile()));
          } else {
            Map<StructureDefinition, List<ValidationMessage>> badProfiles = new HashMap<StructureDefinition, List<ValidationMessage>>();
            Map<StructureDefinition, List<ValidationMessage>> goodProfiles = new HashMap<StructureDefinition, List<ValidationMessage>>();
            int goodCount = 0;
            for (StructureDefinition pr : profiles) {
              List<ValidationMessage> profileErrors = new ArrayList<ValidationMessage>();
              validateResource(we.hostContext(hostContext, pr), profileErrors, we.getResource(), we.getFocus(), pr, IdStatus.OPTIONAL, we.getStack());
              if (!hasErrors(profileErrors)) {
                goodCount++;
                goodProfiles.put(pr, profileErrors);
                trackUsage(pr, hostContext, element);
              } else {
                badProfiles.put(pr, profileErrors);
              }
            }
            if (goodCount == 1) {
              if (showMessagesFromReferences) {
                for (ValidationMessage vm : goodProfiles.values().iterator().next()) {
                  if (!errors.contains(vm)) {
                    errors.add(vm);
                  }
                }
              }

            } else if (goodProfiles.size() == 0) {
              if (!isShowMessagesFromReferences()) {
                rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, areAllBaseProfiles(profiles), I18nConstants.REFERENCE_REF_CANTMATCHCHOICE, ref, asList(type.getTargetProfile()));
                for (StructureDefinition sd : badProfiles.keySet()) {
                  slicingHint(errors, IssueType.STRUCTURE, element.line(), element.col(), path, false,
                    context.formatMessage(I18nConstants.DETAILS_FOR__MATCHING_AGAINST_PROFILE_, ref, sd.getUrl()), errorSummaryForSlicingAsHtml(badProfiles.get(sd)));
                }
              } else {
                rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, profiles.size() == 1, I18nConstants.REFERENCE_REF_CANTMATCHCHOICE, ref, asList(type.getTargetProfile()));
                for (List<ValidationMessage> messages : badProfiles.values()) {
                  for (ValidationMessage vm : messages) {
                    if (!errors.contains(vm)) {
                      errors.add(vm);
                    }
                  }
                }
              }
            } else {
              if (!isShowMessagesFromReferences()) {
                warning(errors, IssueType.STRUCTURE, element.line(), element.col(), path, false, I18nConstants.REFERENCE_REF_MULTIPLEMATCHES, ref, asListByUrl(goodProfiles.keySet()));
                for (StructureDefinition sd : badProfiles.keySet()) {
                  slicingHint(errors, IssueType.STRUCTURE, element.line(), element.col(), path, false, context.formatMessage(I18nConstants.DETAILS_FOR__MATCHING_AGAINST_PROFILE_, ref, sd.getUrl()), errorSummaryForSlicingAsHtml(badProfiles.get(sd)));
                }
              } else {
                warning(errors, IssueType.STRUCTURE, element.line(), element.col(), path, false, I18nConstants.REFERENCE_REF_MULTIPLEMATCHES, ref, asListByUrl(goodProfiles.keySet()));
                for (List<ValidationMessage> messages : goodProfiles.values()) {
                  for (ValidationMessage vm : messages) {
                    if (!errors.contains(vm)) {
                      errors.add(vm);
                    }
                  }
                }
              }
            }
          }
          rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, ok, I18nConstants.REFERENCE_REF_BADTARGETTYPE, ft, types.toString());
        }
        if (type.hasAggregation()) {
          boolean modeOk = false;
          for (Enumeration<AggregationMode> mode : type.getAggregation()) {
            if (mode.getValue().equals(AggregationMode.CONTAINED) && refType.equals("contained"))
              modeOk = true;
            else if (mode.getValue().equals(AggregationMode.BUNDLED) && refType.equals("bundled"))
              modeOk = true;
            else if (mode.getValue().equals(AggregationMode.REFERENCED) && (refType.equals("bundled") || refType.equals("remote")))
              modeOk = true;
          }
          rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, modeOk, I18nConstants.REFERENCE_REF_AGGREGATION, refType);
        }
      }
    }
    if (we == null) {
      TypeRefComponent type = getReferenceTypeRef(container.getType());
      boolean okToRef = !type.hasAggregation() || type.hasAggregation(AggregationMode.REFERENCED);
      rule(errors, IssueType.REQUIRED, -1, -1, path, okToRef, I18nConstants.REFERENCE_REF_NOTFOUND_BUNDLE, ref);
    }
    if (we == null && ft != null && assumeValidRestReferences) {
      // if we == null, we inferred ft from the reference. if we are told to treat this as gospel
      TypeRefComponent type = getReferenceTypeRef(container.getType());
      Set<String> types = new HashSet<>();
      for (CanonicalType tp : type.getTargetProfile()) {
        StructureDefinition sd = context.fetchResource(StructureDefinition.class, tp.getValue());
        if (sd != null) {
          types.add(sd.getType());
        }
      }
      rule(errors, IssueType.STRUCTURE, element.line(), element.col(), path, types.isEmpty() || types.contains(ft), I18nConstants.REFERENCE_REF_BADTARGETTYPE2, ft, ref, types);

    }
    if (pol == ReferenceValidationPolicy.CHECK_VALID) {
      // todo....
    }
  }

  private String asListByUrl(Collection<StructureDefinition> list) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (StructureDefinition sd : list) {
      b.append(sd.getUrl());
    }
    return b.toString();
  }

  private String asList(Collection<CanonicalType> list) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (CanonicalType c : list) {
      b.append(c.getValue());
    }
    return b.toString();
  }

  private boolean areAllBaseProfiles(List<StructureDefinition> profiles) {
    for (StructureDefinition sd : profiles) {
      if (!sd.getUrl().startsWith("http://hl7.org/fhir/StructureDefinition/")) {
        return false;
      }
    }
    return true;
  }

  private String errorSummaryForSlicing(List<ValidationMessage> list) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (ValidationMessage vm : list) {
      if (vm.getLevel() == IssueSeverity.ERROR || vm.getLevel() == IssueSeverity.FATAL || vm.isSlicingHint()) {
        b.append(vm.getLocation() + ": " + vm.getMessage());
      }
    }
    return b.toString();
  }

  private String errorSummaryForSlicingAsHtml(List<ValidationMessage> list) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (ValidationMessage vm : list) {
      if (vm.isSlicingHint()) {
        b.append("<li>" + vm.getLocation() + ": " + vm.getSliceHtml() + "</li>");
      } else if (vm.getLevel() == IssueSeverity.ERROR || vm.getLevel() == IssueSeverity.FATAL) {
        b.append("<li>" + vm.getLocation() + ": " + vm.getHtml() + "</li>");
      }
    }
    return "<ul>" + b.toString() + "</ul>";
  }

  private TypeRefComponent getReferenceTypeRef(List<TypeRefComponent> types) {
    for (TypeRefComponent tr : types) {
      if ("Reference".equals(tr.getCode())) {
        return tr;
      }
    }
    return null;
  }

  private String checkResourceType(String type) {
    long t = System.nanoTime();
    try {
      if (context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + type) != null)
        return type;
      else
        return null;
    } finally {
      sdTime = sdTime + (System.nanoTime() - t);
    }
  }

  private void checkSampledData(List<ValidationMessage> errors, String path, Element focus, SampledData fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".origin", focus.getNamedChild("origin"), fixed.getOrigin(), fixedSource, "origin", focus, pattern);
    checkFixedValue(errors, path + ".period", focus.getNamedChild("period"), fixed.getPeriodElement(), fixedSource, "period", focus, pattern);
    checkFixedValue(errors, path + ".factor", focus.getNamedChild("factor"), fixed.getFactorElement(), fixedSource, "factor", focus, pattern);
    checkFixedValue(errors, path + ".lowerLimit", focus.getNamedChild("lowerLimit"), fixed.getLowerLimitElement(), fixedSource, "lowerLimit", focus, pattern);
    checkFixedValue(errors, path + ".upperLimit", focus.getNamedChild("upperLimit"), fixed.getUpperLimitElement(), fixedSource, "upperLimit", focus, pattern);
    checkFixedValue(errors, path + ".dimensions", focus.getNamedChild("dimensions"), fixed.getDimensionsElement(), fixedSource, "dimensions", focus, pattern);
    checkFixedValue(errors, path + ".data", focus.getNamedChild("data"), fixed.getDataElement(), fixedSource, "data", focus, pattern);
  }

  private void checkTiming(List<ValidationMessage> errors, String path, Element focus, Timing fixed, String fixedSource, boolean pattern) {
    checkFixedValue(errors, path + ".repeat", focus.getNamedChild("repeat"), fixed.getRepeat(), fixedSource, "value", focus, pattern);

    List<Element> events = new ArrayList<Element>();
    focus.getNamedChildren("event", events);
    if (rule(errors, IssueType.VALUE, focus.line(), focus.col(), path, events.size() == fixed.getEvent().size(), I18nConstants.BUNDLE_MSG_EVENT_COUNT, Integer.toString(fixed.getEvent().size()), Integer.toString(events.size()))) {
      for (int i = 0; i < events.size(); i++)
        checkFixedValue(errors, path + ".event", events.get(i), fixed.getEvent().get(i), fixedSource, "event", focus, pattern);
    }
  }

  private boolean codeinExpansion(ValueSetExpansionContainsComponent cnt, String system, String code) {
    for (ValueSetExpansionContainsComponent c : cnt.getContains()) {
      if (code.equals(c.getCode()) && system.equals(c.getSystem().toString()))
        return true;
      if (codeinExpansion(c, system, code))
        return true;
    }
    return false;
  }

  private boolean codeInExpansion(ValueSet vs, String system, String code) {
    for (ValueSetExpansionContainsComponent c : vs.getExpansion().getContains()) {
      if (code.equals(c.getCode()) && (system == null || system.equals(c.getSystem())))
        return true;
      if (codeinExpansion(c, system, code))
        return true;
    }
    return false;
  }

  private String describeReference(String reference) {
    if (reference == null)
      return "null";
    return reference;
  }

  private String describeReference(String reference, CanonicalResource target) {
    if (reference == null && target == null)
      return "null";
    if (reference == null) {
      return target.getUrl();
    }
    if (target == null) {
      return reference;
    }
    if (reference.equals(target.getUrl())) {
      return reference;
    }
    return reference + "(which actually refers to " + target.getUrl() + ")";
  }

  private String describeTypes(List<TypeRefComponent> types) {
    CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
    for (TypeRefComponent t : types) {
      b.append(t.getWorkingCode());
    }
    return b.toString();
  }

  protected ElementDefinition findElement(StructureDefinition profile, String name) {
    for (ElementDefinition c : profile.getSnapshot().getElement()) {
      if (c.getPath().equals(name)) {
        return c;
      }
    }
    return null;
  }

  public BestPracticeWarningLevel getBestPracticeWarningLevel() {
    return bpWarnings;
  }

  @Override
  public CheckDisplayOption getCheckDisplay() {
    return checkDisplay;
  }

  private ConceptDefinitionComponent getCodeDefinition(ConceptDefinitionComponent c, String code) {
    if (code.equals(c.getCode()))
      return c;
    for (ConceptDefinitionComponent g : c.getConcept()) {
      ConceptDefinitionComponent r = getCodeDefinition(g, code);
      if (r != null)
        return r;
    }
    return null;
  }

  private ConceptDefinitionComponent getCodeDefinition(CodeSystem cs, String code) {
    for (ConceptDefinitionComponent c : cs.getConcept()) {
      ConceptDefinitionComponent r = getCodeDefinition(c, code);
      if (r != null)
        return r;
    }
    return null;
  }

  private IndexedElement getContainedById(Element container, String id) {
    List<Element> contained = new ArrayList<Element>();
    container.getNamedChildren("contained", contained);
    for (int i = 0; i < contained.size(); i++) {
      Element we = contained.get(i);
      if (id.equals(we.getNamedChildValue("id"))) {
        return new IndexedElement(i, we, null);
      }
    }
    return null;
  }

  public IWorkerContext getContext() {
    return context;
  }

  private List<ElementDefinition> getCriteriaForDiscriminator(String path, ElementDefinition element, String discriminator, StructureDefinition profile, boolean removeResolve) throws FHIRException {
    List<ElementDefinition> elements = new ArrayList<ElementDefinition>();
    if ("value".equals(discriminator) && element.hasFixed()) {
      elements.add(element);
      return elements;
    }

    if (removeResolve) {  // if we're doing profile slicing, we don't want to walk into the last resolve.. we need the profile on the source not the target
      if (discriminator.equals("resolve()")) {
        elements.add(element);
        return elements;
      }
      if (discriminator.endsWith(".resolve()"))
        discriminator = discriminator.substring(0, discriminator.length() - 10);
    }

    ElementDefinition ed = null;
    ExpressionNode expr = fpe.parse(fixExpr(discriminator));
    long t2 = System.nanoTime();
    ed = fpe.evaluateDefinition(expr, profile, element);
    sdTime = sdTime + (System.nanoTime() - t2);
    if (ed != null)
      elements.add(ed);

    for (TypeRefComponent type : element.getType()) {
      for (CanonicalType p : type.getProfile()) {
        String id = p.hasExtension(ToolingExtensions.EXT_PROFILE_ELEMENT) ? p.getExtensionString(ToolingExtensions.EXT_PROFILE_ELEMENT) : null;
        StructureDefinition sd = context.fetchResource(StructureDefinition.class, p.getValue());
        if (sd == null)
          throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_PROFILE_, p));
        profile = sd;
        if (id == null)
          element = sd.getSnapshot().getElementFirstRep();
        else {
          element = null;
          for (ElementDefinition t : sd.getSnapshot().getElement()) {
            if (id.equals(t.getId()))
              element = t;
          }
          if (element == null)
            throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_ELEMENT__IN_PROFILE_, id, p));
        }
        expr = fpe.parse(fixExpr(discriminator));
        t2 = System.nanoTime();
        ed = fpe.evaluateDefinition(expr, profile, element);
        sdTime = sdTime + (System.nanoTime() - t2);
        if (ed != null)
          elements.add(ed);
      }
    }
    return elements;
  }


  private Element getExtensionByUrl(List<Element> extensions, String urlSimple) {
    for (Element e : extensions) {
      if (urlSimple.equals(e.getNamedChildValue("url")))
        return e;
    }
    return null;
  }

  public List<String> getExtensionDomains() {
    return extensionDomains;
  }

  private IndexedElement getFromBundle(Element bundle, String ref, String fullUrl, List<ValidationMessage> errors, String path, String type, boolean isTransaction) {
    String targetUrl = null;
    String version = "";
    String resourceType = null;
    if (ref.startsWith("http") || ref.startsWith("urn")) {
      // We've got an absolute reference, no need to calculate
      if (ref.contains("/_history/")) {
        targetUrl = ref.substring(0, ref.indexOf("/_history/") - 1);
        version = ref.substring(ref.indexOf("/_history/") + 10);
      } else
        targetUrl = ref;

    } else if (fullUrl == null) {
      //This isn't a problem for signatures - if it's a signature, we won't have a resolution for a relative reference.  For anything else, this is an error
      // but this rule doesn't apply for batches or transactions
      rule(errors, IssueType.REQUIRED, -1, -1, path, Utilities.existsInList(type, "batch-response", "transaction-response") || path.startsWith("Bundle.signature"), I18nConstants.BUNDLE_BUNDLE_FULLURL_MISSING);
      return null;

    } else if (ref.split("/").length != 2 && ref.split("/").length != 4) {
      if (isTransaction) {
        rule(errors, IssueType.INVALID, -1, -1, path, isSearchUrl(ref), I18nConstants.REFERENCE_REF_FORMAT1, ref);
      } else {
        rule(errors, IssueType.INVALID, -1, -1, path, false, I18nConstants.REFERENCE_REF_FORMAT2, ref);
      }
      return null;

    } else {
      String base = "";
      if (fullUrl.startsWith("urn")) {
        String[] parts = fullUrl.split("\\:");
        for (int i = 0; i < parts.length - 1; i++) {
          base = base + parts[i] + ":";
        }
      } else {
        String[] parts;
        parts = fullUrl.split("/");
        for (int i = 0; i < parts.length - 2; i++) {
          base = base + parts[i] + "/";
        }
      }

      String id = null;
      if (ref.contains("/_history/")) {
        version = ref.substring(ref.indexOf("/_history/") + 10);
        String[] refBaseParts = ref.substring(0, ref.indexOf("/_history/")).split("/");
        resourceType = refBaseParts[0];
        id = refBaseParts[1];
      } else if (base.startsWith("urn")) {
        resourceType = ref.split("/")[0];
        id = ref.split("/")[1];
      } else
        id = ref;

      targetUrl = base + id;
    }

    List<Element> entries = new ArrayList<Element>();
    bundle.getNamedChildren("entry", entries);
    Element match = null;
    int matchIndex = -1;
    for (int i = 0; i < entries.size(); i++) {
      Element we = entries.get(i);
      if (targetUrl.equals(we.getChildValue("fullUrl"))) {
        Element r = we.getNamedChild("resource");
        if (version.isEmpty()) {
          rule(errors, IssueType.FORBIDDEN, -1, -1, path, match == null, I18nConstants.BUNDLE_BUNDLE_MULTIPLEMATCHES, ref);
          match = r;
          matchIndex = i;
        } else {
          try {
            if (version.equals(r.getChildren("meta").get(0).getChildValue("versionId"))) {
              rule(errors, IssueType.FORBIDDEN, -1, -1, path, match == null, I18nConstants.BUNDLE_BUNDLE_MULTIPLEMATCHES, ref);
              match = r;
              matchIndex = i;
            }
          } catch (Exception e) {
            warning(errors, IssueType.REQUIRED, -1, -1, path, r.getChildren("meta").size() == 1 && r.getChildren("meta").get(0).getChildValue("versionId") != null, I18nConstants.BUNDLE_BUNDLE_FULLURL_NEEDVERSION, targetUrl);
            // If one of these things is null
          }
        }
      }
    }

    if (match != null && resourceType != null)
      rule(errors, IssueType.REQUIRED, -1, -1, path, match.getType().equals(resourceType), I18nConstants.REFERENCE_REF_RESOURCETYPE, ref, match.getType());
    if (match == null)
      warning(errors, IssueType.REQUIRED, -1, -1, path, !ref.startsWith("urn"), I18nConstants.BUNDLE_BUNDLE_NOT_LOCAL, ref);
    return match == null ? null : new IndexedElement(matchIndex, match, entries.get(matchIndex));
  }

  private boolean isSearchUrl(String ref) {
    if (Utilities.noString(ref) || !ref.contains("?")) {
      return false;
    }
    String tn = ref.substring(0, ref.indexOf("?"));
    String q = ref.substring(ref.indexOf("?") + 1);
    if (!context.getResourceNames().contains(tn)) {
      return false;
    } else {
      return q.matches("([_a-zA-Z][_a-zA-Z0-9]*=[^=&]+)(&([_a-zA-Z][_a-zA-Z0-9]*=[^=&]+))*");
    }
  }

  private StructureDefinition getProfileForType(String type, List<TypeRefComponent> list) {
    for (TypeRefComponent tr : list) {
      String url = tr.getWorkingCode();
      if (!Utilities.isAbsoluteUrl(url))
        url = "http://hl7.org/fhir/StructureDefinition/" + url;
      long t = System.nanoTime();
      StructureDefinition sd = context.fetchResource(StructureDefinition.class, url);
      sdTime = sdTime + (System.nanoTime() - t);
      if (sd != null && (sd.getType().equals(type) || sd.getUrl().equals(type)) && sd.hasSnapshot())
        return sd;
    }
    return null;
  }

  private Element getValueForDiscriminator(Object appContext, List<ValidationMessage> errors, Element element, String discriminator, ElementDefinition criteria, NodeStack stack) throws FHIRException, IOException {
    String p = stack.getLiteralPath() + "." + element.getName();
    Element focus = element;
    String[] dlist = discriminator.split("\\.");
    for (String d : dlist) {
      if (focus.fhirType().equals("Reference") && d.equals("reference")) {
        String url = focus.getChildValue("reference");
        if (Utilities.noString(url))
          throw new FHIRException(context.formatMessage(I18nConstants.NO_REFERENCE_RESOLVING_DISCRIMINATOR__FROM_, discriminator, element.getProperty().getName()));
        // Note that we use the passed in stack here. This might be a problem if the discriminator is deep enough?
        Element target = resolve(appContext, url, stack, errors, p);
        if (target == null)
          throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_FIND_RESOURCE__AT__RESOLVING_DISCRIMINATOR__FROM_, url, d, discriminator, element.getProperty().getName()));
        focus = target;
      } else if (d.equals("value") && focus.isPrimitive()) {
        return focus;
      } else {
        List<Element> children = focus.getChildren(d);
        if (children.isEmpty())
          throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_FIND__RESOLVING_DISCRIMINATOR__FROM_, d, discriminator, element.getProperty().getName()));
        if (children.size() > 1)
          throw new FHIRException(context.formatMessage(I18nConstants.FOUND__ITEMS_FOR__RESOLVING_DISCRIMINATOR__FROM_, Integer.toString(children.size()), d, discriminator, element.getProperty().getName()));
        focus = children.get(0);
        p = p + "." + d;
      }
    }
    return focus;
  }

  private CodeSystem getCodeSystem(String system) {
    long t = System.nanoTime();
    try {
      return context.fetchCodeSystem(system);
    } finally {
      txTime = txTime + (System.nanoTime() - t);
    }
  }

  private boolean hasTime(String fmt) {
    return fmt.contains("T");
  }

  private boolean hasTimeZone(String fmt) {
    return fmt.length() > 10 && (fmt.substring(10).contains("-") || fmt.substring(10).contains("+") || fmt.substring(10).contains("Z"));
  }

  private boolean isAbsolute(String uri) {
    return Utilities.noString(uri) || uri.startsWith("http:") || uri.startsWith("https:") || uri.startsWith("urn:uuid:") || uri.startsWith("urn:oid:") || uri.startsWith("urn:ietf:")
      || uri.startsWith("urn:iso:") || uri.startsWith("urn:iso-astm:") || isValidFHIRUrn(uri);
  }

  private boolean isValidFHIRUrn(String uri) {
    return (uri.equals("urn:x-fhir:uk:id:nhs-number")) || uri.startsWith("urn:"); // Anyone can invent a URN, so why should we complain?
  }

  public boolean isAnyExtensionsAllowed() {
    return anyExtensionsAllowed;
  }

  public boolean isErrorForUnknownProfiles() {
    return errorForUnknownProfiles;
  }

  public void setErrorForUnknownProfiles(boolean errorForUnknownProfiles) {
    this.errorForUnknownProfiles = errorForUnknownProfiles;
  }

  private boolean isParametersEntry(String path) {
    String[] parts = path.split("\\.");
    return parts.length > 2 && parts[parts.length - 1].equals("resource") && (pathEntryHasName(parts[parts.length - 2], "parameter") || pathEntryHasName(parts[parts.length - 2], "part"));
  }

  private boolean isBundleEntry(String path) {
    String[] parts = path.split("\\.");
    return parts.length > 2 && parts[parts.length - 1].equals("resource") && pathEntryHasName(parts[parts.length - 2], "entry");
  }

  private boolean isBundleOutcome(String path) {
    String[] parts = path.split("\\.");
    return parts.length > 2 && parts[parts.length - 1].equals("outcome") && pathEntryHasName(parts[parts.length - 2], "response");
  }


  private static boolean pathEntryHasName(String thePathEntry, String theName) {
    if (thePathEntry.equals(theName)) {
      return true;
    }
    if (thePathEntry.length() >= theName.length() + 3) {
      if (thePathEntry.startsWith(theName)) {
        if (thePathEntry.charAt(theName.length()) == '[') {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isPrimitiveType(String code) {
    StructureDefinition sd = context.fetchTypeDefinition(code);
    return sd != null && sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE;
  }

  private String getErrorMessage(String message) {
    return message != null ? " (error message = " + message + ")" : "";
  }

  public boolean isSuppressLoincSnomedMessages() {
    return suppressLoincSnomedMessages;
  }

  private boolean nameMatches(String name, String tail) {
    if (tail.endsWith("[x]"))
      return name.startsWith(tail.substring(0, tail.length() - 3));
    else
      return (name.equals(tail));
  }

  private boolean passesCodeWhitespaceRules(String v) {
    if (!v.trim().equals(v))
      return false;
    boolean lastWasSpace = true;
    for (char c : v.toCharArray()) {
      if (c == ' ') {
        if (lastWasSpace)
          return false;
        else
          lastWasSpace = true;
      } else if (Character.isWhitespace(c))
        return false;
      else
        lastWasSpace = false;
    }
    return true;
  }

  private ResolvedReference localResolve(String ref, NodeStack stack, List<ValidationMessage> errors, String path, Element hostContext, Element source) {
    if (ref.startsWith("#")) {
      // work back through the parent list.
      // really, there should only be one level for this (contained resources cannot contain
      // contained resources), but we'll leave that to some other code to worry about
      while (stack != null && stack.getElement() != null) {
        if (stack.getElement().getProperty().isResource()) {
          // ok, we'll try to find the contained reference
          IndexedElement res = getContainedById(stack.getElement(), ref.substring(1));
          if (res != null) {
            ResolvedReference rr = new ResolvedReference();
            rr.setResource(stack.getElement());
            rr.setFocus(res.getMatch());
            rr.setExternal(false);
            rr.setStack(stack.push(res.getMatch(), res.getIndex(), res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition()));
            return rr;
          }
        }
        if (stack.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY) {
          return null; // we don't try to resolve contained references across this boundary
        }
        stack = stack.parent;
      }
      return null;
    } else {
      // work back through the parent list - if any of them are bundles, try to resolve
      // the resource in the bundle
      String fullUrl = null; // we're going to try to work this out as we go up
      while (stack != null && stack.getElement() != null) {
        if (stack.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY && fullUrl == null && stack.parent != null && stack.parent.getElement().getName().equals("entry")) {
          String type = stack.parent.parent.element.getChildValue("type");
          fullUrl = stack.parent.getElement().getChildValue("fullUrl"); // we don't try to resolve contained references across this boundary
          if (fullUrl == null)
            rule(errors, IssueType.REQUIRED, stack.parent.getElement().line(), stack.parent.getElement().col(), stack.parent.getLiteralPath(),
              Utilities.existsInList(type, "batch-response", "transaction-response") || fullUrl != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFULLURL);
        }
        if ("Bundle".equals(stack.getElement().getType())) {
          String type = stack.getElement().getChildValue("type");
          IndexedElement res = getFromBundle(stack.getElement(), ref, fullUrl, errors, path, type, "transaction".equals(type));
          if (res == null) {
            return null;
          } else {
            ResolvedReference rr = new ResolvedReference();
            rr.setResource(res.getMatch());
            rr.setFocus(res.getMatch());
            rr.setExternal(false);
            rr.setStack(stack.push(res.getEntry(), res.getIndex(), res.getEntry().getProperty().getDefinition(),
              res.getEntry().getProperty().getDefinition()).push(res.getMatch(), -1,
              res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition()));
            return rr;
          }
        }
        stack = stack.parent;
      }
      // we can get here if we got called via FHIRPath conformsTo which breaks the stack continuity.
      if (hostContext != null && "Bundle".equals(hostContext.fhirType())) {
        String type = hostContext.getChildValue("type");
        Element entry = getEntryForSource(hostContext, source);
        fullUrl = entry.getChildValue("fullUrl");
        IndexedElement res = getFromBundle(hostContext, ref, fullUrl, errors, path, type, "transaction".equals(type));
        if (res == null) {
          return null;
        } else {
          ResolvedReference rr = new ResolvedReference();
          rr.setResource(res.getMatch());
          rr.setFocus(res.getMatch());
          rr.setExternal(false);
          rr.setStack(new NodeStack(hostContext).push(res.getEntry(), res.getIndex(), res.getEntry().getProperty().getDefinition(),
            res.getEntry().getProperty().getDefinition()).push(res.getMatch(), -1,
            res.getMatch().getProperty().getDefinition(), res.getMatch().getProperty().getDefinition()));
          return rr;
        }
      }
    }
    return null;
  }

  private Element getEntryForSource(Element bundle, Element element) {
    List<Element> entries = new ArrayList<Element>();
    bundle.getNamedChildren("entry", entries);
    for (Element entry : entries) {
      if (entry.hasDescendant(element)) {
        return entry;
      }
    }
    return null;
  }

  private ResolvedReference makeExternalRef(Element external, String path) {
    ResolvedReference res = new ResolvedReference();
    res.setResource(external);
    res.setFocus(external);
    res.setExternal(true);
    res.setStack(new NodeStack(external, path));
    return res;
  }


  private Element resolve(Object appContext, String ref, NodeStack stack, List<ValidationMessage> errors, String path) throws IOException, FHIRException {
    Element local = localResolve(ref, stack, errors, path, null, null).getFocus();
    if (local != null)
      return local;
    if (fetcher == null)
      return null;
    if (fetchCache.containsKey(ref)) {
      return fetchCache.get(ref);
    } else {
      Element res = fetcher.fetch(appContext, ref);
      fetchCache.put(ref, res);
      return res;
    }
  }

  private ValueSet resolveBindingReference(DomainResource ctxt, String reference, String uri) {
    if (reference != null) {
      if (reference.startsWith("#")) {
        for (Resource c : ctxt.getContained()) {
          if (c.getId().equals(reference.substring(1)) && (c instanceof ValueSet))
            return (ValueSet) c;
        }
        return null;
      } else {
        long t = System.nanoTime();
        ValueSet fr = context.fetchResource(ValueSet.class, reference);
        if (fr == null) {
          if (!Utilities.isAbsoluteUrl(reference)) {
            reference = resolve(uri, reference);
            fr = context.fetchResource(ValueSet.class, reference);
          }
        }
        if (fr == null)
          fr = ValueSetUtilities.generateImplicitValueSet(reference);
        txTime = txTime + (System.nanoTime() - t);
        return fr;
      }
    } else
      return null;
  }

  private String resolve(String uri, String ref) {
    if (isBlank(uri)) {
      return ref;
    }
    String[] up = uri.split("\\/");
    String[] rp = ref.split("\\/");
    if (context.getResourceNames().contains(up[up.length - 2]) && context.getResourceNames().contains(rp[0])) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < up.length - 2; i++) {
        b.append(up[i]);
        b.append("/");
      }
      b.append(ref);
      return b.toString();
    } else
      return ref;
  }

  private Element resolveInBundle(List<Element> entries, String ref, String fullUrl, String type, String id) {
    if (Utilities.isAbsoluteUrl(ref)) {
      // if the reference is absolute, then you resolve by fullUrl. No other thinking is required.
      for (Element entry : entries) {
        String fu = entry.getNamedChildValue("fullUrl");
        if (ref.equals(fu))
          return entry;
      }
      return null;
    } else {
      // split into base, type, and id
      String u = null;
      if (fullUrl != null && fullUrl.endsWith(type + "/" + id))
        // fullUrl = complex
        u = fullUrl.substring(0, fullUrl.length() - (type + "/" + id).length()) + ref;
//        u = fullUrl.substring((type+"/"+id).length())+ref;
      String[] parts = ref.split("\\/");
      if (parts.length >= 2) {
        String t = parts[0];
        String i = parts[1];
        for (Element entry : entries) {
          String fu = entry.getNamedChildValue("fullUrl");
          if (fu != null && fu.equals(u))
            return entry;
          if (u == null) {
            Element resource = entry.getNamedChild("resource");
            if (resource != null) {
              String et = resource.getType();
              String eid = resource.getNamedChildValue("id");
              if (t.equals(et) && i.equals(eid))
                return entry;
            }
          }
        }
      }
      return null;
    }
  }

  private ElementDefinition resolveNameReference(StructureDefinitionSnapshotComponent snapshot, String contentReference) {
    for (ElementDefinition ed : snapshot.getElement())
      if (contentReference.equals("#" + ed.getId()))
        return ed;
    return null;
  }

  private StructureDefinition resolveProfile(StructureDefinition profile, String pr) {
    if (pr.startsWith("#")) {
      for (Resource r : profile.getContained()) {
        if (r.getId().equals(pr.substring(1)) && r instanceof StructureDefinition)
          return (StructureDefinition) r;
      }
      return null;
    } else {
      long t = System.nanoTime();
      StructureDefinition fr = context.fetchResource(StructureDefinition.class, pr);
      sdTime = sdTime + (System.nanoTime() - t);
      return fr;
    }
  }

  private ElementDefinition resolveType(String type, List<TypeRefComponent> list) {
    for (TypeRefComponent tr : list) {
      String url = tr.getWorkingCode();
      if (!Utilities.isAbsoluteUrl(url))
        url = "http://hl7.org/fhir/StructureDefinition/" + url;
      long t = System.nanoTime();
      StructureDefinition sd = context.fetchResource(StructureDefinition.class, url);
      sdTime = sdTime + (System.nanoTime() - t);
      if (sd != null && (sd.getType().equals(type) || sd.getUrl().equals(type)) && sd.hasSnapshot())
        return sd.getSnapshot().getElement().get(0);
    }
    return null;
  }

  public void setAnyExtensionsAllowed(boolean anyExtensionsAllowed) {
    this.anyExtensionsAllowed = anyExtensionsAllowed;
  }

  public IResourceValidator setBestPracticeWarningLevel(BestPracticeWarningLevel value) {
    bpWarnings = value;
    return this;
  }

  @Override
  public void setCheckDisplay(CheckDisplayOption checkDisplay) {
    this.checkDisplay = checkDisplay;
  }

  public void setSuppressLoincSnomedMessages(boolean suppressLoincSnomedMessages) {
    this.suppressLoincSnomedMessages = suppressLoincSnomedMessages;
  }

  public IdStatus getResourceIdRule() {
    return resourceIdRule;
  }

  public void setResourceIdRule(IdStatus resourceIdRule) {
    this.resourceIdRule = resourceIdRule;
  }


  public boolean isAllowXsiLocation() {
    return allowXsiLocation;
  }

  public void setAllowXsiLocation(boolean allowXsiLocation) {
    this.allowXsiLocation = allowXsiLocation;
  }

  /**
   * @param element - the candidate that might be in the slice
   * @param path    - for reporting any errors. the XPath for the element
   * @param slicer  - the definition of how slicing is determined
   * @param ed      - the slice for which to test membership
   * @param errors
   * @param stack
   * @return
   * @throws DefinitionException
   * @throws DefinitionException
   * @throws IOException
   * @throws FHIRException
   */
  private boolean sliceMatches(ValidatorHostContext hostContext, Element element, String path, ElementDefinition slicer, ElementDefinition ed, StructureDefinition profile, List<ValidationMessage> errors, List<ValidationMessage> sliceInfo, NodeStack stack) throws DefinitionException, FHIRException {
    if (!slicer.getSlicing().hasDiscriminator())
      return false; // cannot validate in this case

    ExpressionNode n = (ExpressionNode) ed.getUserData("slice.expression.cache");
    if (n == null) {
      long t = System.nanoTime();
      // GG: this approach is flawed because it treats discriminators individually rather than collectively
      StringBuilder expression = new StringBuilder("true");
      boolean anyFound = false;
      Set<String> discriminators = new HashSet<>();
      for (ElementDefinitionSlicingDiscriminatorComponent s : slicer.getSlicing().getDiscriminator()) {
        String discriminator = s.getPath();
        discriminators.add(discriminator);

        List<ElementDefinition> criteriaElements = getCriteriaForDiscriminator(path, ed, discriminator, profile, s.getType() == DiscriminatorType.PROFILE);
        boolean found = false;
        for (ElementDefinition criteriaElement : criteriaElements) {
          found = true;
          if (s.getType() == DiscriminatorType.TYPE) {
            String type = null;
            if (!criteriaElement.getPath().contains("[") && discriminator.contains("[")) {
              discriminator = discriminator.substring(0, discriminator.indexOf('['));
              String lastNode = tail(discriminator);
              type = tail(criteriaElement.getPath()).substring(lastNode.length());
              type = type.substring(0, 1).toLowerCase() + type.substring(1);
            } else if (!criteriaElement.hasType() || criteriaElement.getType().size() == 1) {
              if (discriminator.contains("["))
                discriminator = discriminator.substring(0, discriminator.indexOf('['));
              type = criteriaElement.getType().get(0).getWorkingCode();
            } else if (criteriaElement.getType().size() > 1) {
              throw new DefinitionException(context.formatMessage(I18nConstants.DISCRIMINATOR__IS_BASED_ON_TYPE_BUT_SLICE__IN__HAS_MULTIPLE_TYPES_, discriminator, ed.getId(), profile.getUrl(), criteriaElement.typeSummary()));
            } else
              throw new DefinitionException(context.formatMessage(I18nConstants.DISCRIMINATOR__IS_BASED_ON_TYPE_BUT_SLICE__IN__HAS_NO_TYPES, discriminator, ed.getId(), profile.getUrl()));
            if (discriminator.isEmpty())
              expression.append(" and $this is " + type);
            else
              expression.append(" and " + discriminator + " is " + type);
          } else if (s.getType() == DiscriminatorType.PROFILE) {
            if (criteriaElement.getType().size() == 0) {
              throw new DefinitionException(context.formatMessage(I18nConstants.PROFILE_BASED_DISCRIMINATORS_MUST_HAVE_A_TYPE__IN_PROFILE_, criteriaElement.getId(), profile.getUrl()));
            }
            if (criteriaElement.getType().size() != 1) {
              throw new DefinitionException(context.formatMessage(I18nConstants.PROFILE_BASED_DISCRIMINATORS_MUST_HAVE_ONLY_ONE_TYPE__IN_PROFILE_, criteriaElement.getId(), profile.getUrl()));
            }
            List<CanonicalType> list = discriminator.endsWith(".resolve()") || discriminator.equals("resolve()") ? criteriaElement.getType().get(0).getTargetProfile() : criteriaElement.getType().get(0).getProfile();
            if (list.size() == 0) {
              throw new DefinitionException(context.formatMessage(I18nConstants.PROFILE_BASED_DISCRIMINATORS_MUST_HAVE_A_TYPE_WITH_A_PROFILE__IN_PROFILE_, criteriaElement.getId(), profile.getUrl()));
            } else if (list.size() > 1) {
              CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder(" or ");
              for (CanonicalType c : list) {
                b.append(discriminator + ".conformsTo('" + c.getValue() + "')");
              }
              expression.append(" and (" + b + ")");
            } else {
              expression.append(" and " + discriminator + ".conformsTo('" + list.get(0).getValue() + "')");
            }
          } else if (s.getType() == DiscriminatorType.EXISTS) {
            if (criteriaElement.hasMin() && criteriaElement.getMin() >= 1)
              expression.append(" and (" + discriminator + ".exists())");
            else if (criteriaElement.hasMax() && criteriaElement.getMax().equals("0"))
              expression.append(" and (" + discriminator + ".exists().not())");
            else
              throw new FHIRException(context.formatMessage(I18nConstants.DISCRIMINATOR__IS_BASED_ON_ELEMENT_EXISTENCE_BUT_SLICE__NEITHER_SETS_MIN1_OR_MAX0, discriminator, ed.getId()));
          } else if (criteriaElement.hasFixed()) {
            buildFixedExpression(ed, expression, discriminator, criteriaElement);
          } else if (criteriaElement.hasPattern()) {
            buildPattternExpression(ed, expression, discriminator, criteriaElement);
          } else if (criteriaElement.hasBinding() && criteriaElement.getBinding().hasStrength() && criteriaElement.getBinding().getStrength().equals(BindingStrength.REQUIRED) && criteriaElement.getBinding().hasValueSet()) {
            expression.append(" and (" + discriminator + " memberOf '" + criteriaElement.getBinding().getValueSet() + "')");
          } else {
            found = false;
          }
          if (found)
            break;
        }
        if (found)
          anyFound = true;
      }
      if (!anyFound) {
        if (slicer.getSlicing().getDiscriminator().size() > 1)
          throw new DefinitionException(context.formatMessage(I18nConstants.COULD_NOT_MATCH_ANY_DISCRIMINATORS__FOR_SLICE__IN_PROFILE___NONE_OF_THE_DISCRIMINATOR__HAVE_FIXED_VALUE_BINDING_OR_EXISTENCE_ASSERTIONS, discriminators, ed.getId(), profile.getUrl(), discriminators));
        else
          throw new DefinitionException(context.formatMessage(I18nConstants.COULD_NOT_MATCH_DISCRIMINATOR__FOR_SLICE__IN_PROFILE___THE_DISCRIMINATOR__DOES_NOT_HAVE_FIXED_VALUE_BINDING_OR_EXISTENCE_ASSERTIONS, discriminators, ed.getId(), profile.getUrl(), discriminators));
      }

      try {
        n = fpe.parse(fixExpr(expression.toString()));
      } catch (FHIRLexerException e) {
        throw new FHIRException(context.formatMessage(I18nConstants.PROBLEM_PROCESSING_EXPRESSION__IN_PROFILE__PATH__, expression, profile.getUrl(), path, e.getMessage()));
      }
      fpeTime = fpeTime + (System.nanoTime() - t);
      ed.setUserData("slice.expression.cache", n);
    }

    ValidatorHostContext shc = hostContext.forSlicing();
    boolean pass = evaluateSlicingExpression(shc, element, path, profile, n);
    if (!pass) {
      slicingHint(sliceInfo, IssueType.STRUCTURE, element.line(), element.col(), path, false, (context.formatMessage(I18nConstants.DOES_NOT_MATCH_SLICE_, ed.getSliceName())), "discriminator = " + Utilities.escapeXml(n.toString()));
      for (String url : shc.getSliceRecords().keySet()) {
        slicingHint(sliceInfo, IssueType.STRUCTURE, element.line(), element.col(), path, false,
         context.formatMessage(I18nConstants.DETAILS_FOR__MATCHING_AGAINST_PROFILE_, stack.getLiteralPath(), url),
          context.formatMessage(I18nConstants.PROFILE__DOES_NOT_MATCH_FOR__BECAUSE_OF_THE_FOLLOWING_PROFILE_ISSUES__,
              url,
              stack.getLiteralPath(), errorSummaryForSlicingAsHtml(shc.getSliceRecords().get(url))));
      }
    }
    return pass;
  }

  public boolean evaluateSlicingExpression(ValidatorHostContext hostContext, Element element, String path, StructureDefinition profile, ExpressionNode n) throws FHIRException {
    String msg;
    boolean ok;
    try {
      long t = System.nanoTime();
      ok = fpe.evaluateToBoolean(hostContext.forProfile(profile), hostContext.getResource(), hostContext.getRootResource(), element, n);
      fpeTime = fpeTime + (System.nanoTime() - t);
      msg = fpe.forLog();
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new FHIRException(context.formatMessage(I18nConstants.PROBLEM_EVALUATING_SLICING_EXPRESSION_FOR_ELEMENT_IN_PROFILE__PATH__FHIRPATH___, profile.getUrl(), path, n, ex.getMessage()));
    }
    return ok;
  }

  private void buildPattternExpression(ElementDefinition ed, StringBuilder expression, String discriminator, ElementDefinition criteriaElement) throws DefinitionException {
    DataType pattern = criteriaElement.getPattern();
    if (pattern instanceof CodeableConcept) {
      CodeableConcept cc = (CodeableConcept) pattern;
      expression.append(" and ");
      buildCodeableConceptExpression(ed, expression, discriminator, cc);
    } else if (pattern instanceof Coding) {
      Coding c = (Coding) pattern;
      expression.append(" and ");
      buildCodingExpression(ed, expression, discriminator, c);
    } else if (pattern instanceof Identifier) {
      Identifier ii = (Identifier) pattern;
      expression.append(" and ");
      buildIdentifierExpression(ed, expression, discriminator, ii);
    } else {
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_FIXED_PATTERN_TYPE_FOR_DISCRIMINATOR_FOR_SLICE__, discriminator, ed.getId(), pattern.getClass().getName()));
    }
  }

  private void buildIdentifierExpression(ElementDefinition ed, StringBuilder expression, String discriminator, Identifier ii)
    throws DefinitionException {
    if (ii.hasExtension())
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_IDENTIFIER_PATTERN__EXTENSIONS_ARE_NOT_ALLOWED__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
    boolean first = true;
    expression.append(discriminator + ".where(");
    if (ii.hasSystem()) {
      first = false;
      expression.append("system = '" + ii.getSystem() + "'");
    }
    if (ii.hasValue()) {
      if (first)
        first = false;
      else
        expression.append(" and ");
      expression.append("value = '" + ii.getValue() + "'");
    }
    if (ii.hasUse()) {
      if (first)
        first = false;
      else
        expression.append(" and ");
      expression.append("use = '" + ii.getUse() + "'");
    }
    if (ii.hasType()) {
      if (first)
        first = false;
      else
        expression.append(" and ");
      buildCodeableConceptExpression(ed, expression, "type", ii.getType());
    }
    expression.append(").exists()");
  }

  private void buildCodeableConceptExpression(ElementDefinition ed, StringBuilder expression, String discriminator, CodeableConcept cc)
    throws DefinitionException {
    if (cc.hasText())
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_CODEABLECONCEPT_PATTERN__USING_TEXT__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
    if (!cc.hasCoding())
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_CODEABLECONCEPT_PATTERN__MUST_HAVE_AT_LEAST_ONE_CODING__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
    if (cc.hasExtension())
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_CODEABLECONCEPT_PATTERN__EXTENSIONS_ARE_NOT_ALLOWED__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
    boolean firstCoding = true;
    for (Coding c : cc.getCoding()) {
      if (c.hasExtension())
        throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_CODEABLECONCEPT_PATTERN__EXTENSIONS_ARE_NOT_ALLOWED__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
      if (firstCoding) firstCoding = false;
      else expression.append(" and ");
      expression.append(discriminator + ".coding.where(");
      boolean first = true;
      if (c.hasSystem()) {
        first = false;
        expression.append("system = '" + c.getSystem() + "'");
      }
      if (c.hasVersion()) {
        if (first) first = false;
        else expression.append(" and ");
        expression.append("version = '" + c.getVersion() + "'");
      }
      if (c.hasCode()) {
        if (first) first = false;
        else expression.append(" and ");
        expression.append("code = '" + c.getCode() + "'");
      }
      if (c.hasDisplay()) {
        if (first) first = false;
        else expression.append(" and ");
        expression.append("display = '" + c.getDisplay() + "'");
      }
      expression.append(").exists()");
    }
  }

  private void buildCodingExpression(ElementDefinition ed, StringBuilder expression, String discriminator, Coding c)
    throws DefinitionException {
    if (c.hasExtension())
      throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_CODEABLECONCEPT_PATTERN__EXTENSIONS_ARE_NOT_ALLOWED__FOR_DISCRIMINATOR_FOR_SLICE_, discriminator, ed.getId()));
    expression.append(discriminator + ".where(");
    boolean first = true;
    if (c.hasSystem()) {
      first = false;
      expression.append("system = '" + c.getSystem() + "'");
    }
    if (c.hasVersion()) {
      if (first) first = false;
      else expression.append(" and ");
      expression.append("version = '" + c.getVersion() + "'");
    }
    if (c.hasCode()) {
      if (first) first = false;
      else expression.append(" and ");
      expression.append("code = '" + c.getCode() + "'");
    }
    if (c.hasDisplay()) {
      if (first) first = false;
      else expression.append(" and ");
      expression.append("display = '" + c.getDisplay() + "'");
    }
    expression.append(").exists()");
  }

  private void buildFixedExpression(ElementDefinition ed, StringBuilder expression, String discriminator, ElementDefinition criteriaElement) throws DefinitionException {
    DataType fixed = criteriaElement.getFixed();
    if (fixed instanceof CodeableConcept) {
      CodeableConcept cc = (CodeableConcept) fixed;
      expression.append(" and ");
      buildCodeableConceptExpression(ed, expression, discriminator, cc);
    } else if (fixed instanceof Identifier) {
      Identifier ii = (Identifier) fixed;
      expression.append(" and ");
      buildIdentifierExpression(ed, expression, discriminator, ii);
    } else if (fixed instanceof Coding) {
      Coding c = (Coding) fixed;
      expression.append(" and ");
      buildCodingExpression(ed, expression, discriminator, c);
    } else {
      expression.append(" and (");
      if (fixed instanceof StringType) {
        Gson gson = new Gson();
        String json = gson.toJson((StringType) fixed);
        String escapedString = json.substring(json.indexOf(":") + 2);
        escapedString = escapedString.substring(0, escapedString.indexOf(",\"myStringValue") - 1);
        expression.append("'" + escapedString + "'");
      } else if (fixed instanceof UriType) {
        expression.append("'" + ((UriType) fixed).asStringValue() + "'");
      } else if (fixed instanceof IntegerType) {
        expression.append(((IntegerType) fixed).asStringValue());
      } else if (fixed instanceof DecimalType) {
        expression.append(((IntegerType) fixed).asStringValue());
      } else if (fixed instanceof BooleanType) {
        expression.append(((BooleanType) fixed).asStringValue());
      } else
        throw new DefinitionException(context.formatMessage(I18nConstants.UNSUPPORTED_FIXED_VALUE_TYPE_FOR_DISCRIMINATOR_FOR_SLICE__, discriminator, ed.getId(), fixed.getClass().getName()));
      expression.append(" in " + discriminator + ")");
    }
  }

  // checkSpecials = we're only going to run these tests if we are actually validating this content (as opposed to we looked it up)
  private void start(ValidatorHostContext hostContext, List<ValidationMessage> errors, Element resource, Element element, StructureDefinition defn, NodeStack stack) throws FHIRException {
    checkLang(resource, stack);

    if ("Bundle".equals(element.fhirType())) {
      resolveBundleReferences(element, new ArrayList<Element>());
    }
    startInner(hostContext, errors, resource, element, defn, stack, hostContext.isCheckSpecials());

    List<String> res = new ArrayList<>();
    Element meta = element.getNamedChild("meta");
    if (meta != null) {
      List<Element> profiles = new ArrayList<Element>();
      meta.getNamedChildren("profile", profiles);
      int i = 0;
      for (Element profile : profiles) {
        StructureDefinition sd = context.fetchResource(StructureDefinition.class, profile.primitiveValue());
        if (!defn.getUrl().equals(profile.primitiveValue())) {
          if (warning(errors, IssueType.STRUCTURE, element.line(), element.col(), stack.getLiteralPath() + ".meta.profile[" + i + "]", sd != null, I18nConstants.VALIDATION_VAL_PROFILE_UNKNOWN, profile.primitiveValue())) {
            startInner(hostContext, errors, resource, element, sd, stack, false);
          }
        }
        i++;
      }
    }
  }

  private void resolveBundleReferences(Element element, List<Element> bundles) {
    if (!element.hasUserData("validator.bundle.resolved")) {
      element.setUserData("validator.bundle.resolved", true);
      List<Element> list = new ArrayList<Element>();
      list.addAll(bundles);
      list.add(0, element);
      List<Element> entries = element.getChildrenByName("entry");
      for (Element entry : entries) {
        String fu = entry.getChildValue("fullUrl");
        Element r = entry.getNamedChild("resource");
        if (r != null) {
          resolveBundleReferencesInResource(list, r, fu);
        }
      }
    }
  }

  private void resolveBundleReferencesInResource(List<Element> bundles, Element r, String fu) {
    r.setUserData("validator.bundle.resolution-resource", null);
    if ("Bundle".equals(r.fhirType())) {
      resolveBundleReferences(r, bundles);
    } else {
      for (Element child : r.getChildren()) {
        resolveBundleReferencesForElement(bundles, r, fu, child);
      }
    }
  }

  private void resolveBundleReferencesForElement(List<Element> bundles, Element resource, String fu, Element element) {
    if ("Reference".equals(element.fhirType())) {
      String ref = element.getChildValue("reference");
      if (!Utilities.noString(ref)) {
        for (Element bundle : bundles) {
          List<Element> entries = bundle.getChildren("entry");
          Element tgt = resolveInBundle(entries, ref, fu, resource.fhirType(), resource.getIdBase());
          if (tgt != null) {
            element.setUserData("validator.bundle.resolution", tgt.getNamedChild("resource"));
            return;
          }
        }
        element.setUserData("validator.bundle.resolution-failed", ref);
      }
    } else {
      element.setUserData("validator.bundle.resolution-noref", null);
      for (Element child : element.getChildren()) {
        resolveBundleReferencesForElement(bundles, resource, fu, child);
      }
    }

  }

  public void startInner(ValidatorHostContext hostContext, List<ValidationMessage> errors, Element resource, Element element, StructureDefinition defn, NodeStack stack, boolean checkSpecials) {
    // the first piece of business is to see if we've validated this resource against this profile before.
    // if we have (*or if we still are*), then we'll just return our existing errors
    ResourceValidationTracker resTracker = getResourceTracker(element);
    List<ValidationMessage> cachedErrors = resTracker.getOutcomes(defn);
    if (cachedErrors != null) {
      for (ValidationMessage vm : cachedErrors) {
        if (!errors.contains(vm)) {
          errors.add(vm);
        }
      }
      return;
    }
    if (rule(errors, IssueType.STRUCTURE, element.line(), element.col(), stack.getLiteralPath(), defn.hasSnapshot(), I18nConstants.VALIDATION_VAL_PROFILE_NOSNAPSHOT)) {
      List<ValidationMessage> localErrors = new ArrayList<ValidationMessage>();
      resTracker.startValidating(defn);
      trackUsage(defn, hostContext, element);
      validateElement(hostContext, localErrors, defn, defn.getSnapshot().getElement().get(0), null, null, resource, element, element.getName(), stack, false, true, null);
      resTracker.storeOutcomes(defn, localErrors);
      for (ValidationMessage vm : localErrors) {
        if (!errors.contains(vm)) {
          errors.add(vm);
        }
      }
    }
    if (checkSpecials) {
      checkSpecials(hostContext, errors, element, stack, checkSpecials);
      validateResourceRules(errors, element, stack);
    }
  }

  public void checkSpecials(ValidatorHostContext hostContext, List<ValidationMessage> errors, Element element, NodeStack stack, boolean checkSpecials) {
    // specific known special validations
    if (element.getType().equals("Bundle")) {
      validateBundle(errors, element, stack, checkSpecials);
    } else if (element.getType().equals("Observation")) {
      validateObservation(errors, element, stack);
    } else if (element.getType().equals("Questionnaire")) {
      ArrayList<Element> parents = new ArrayList<>();
      parents.add(element);
      validateQuestionannaireItem(errors, element, element, stack, parents);
    } else if (element.getType().equals("QuestionnaireResponse")) {
      validateQuestionannaireResponse(hostContext, errors, element, stack);
    } else if (element.getType().equals("CapabilityStatement")) {
      validateCapabilityStatement(errors, element, stack);
    } else if (element.getType().equals("CodeSystem")) {
      validateCodeSystem(errors, element, stack);
    }
  }

  private ResourceValidationTracker getResourceTracker(Element element) {
    ResourceValidationTracker res = resourceTracker.get(element);
    if (res == null) {
      res = new ResourceValidationTracker();
      resourceTracker.put(element, res);
    }
    return res;
  }

  private void validateQuestionannaireItem(List<ValidationMessage> errors, Element element, Element questionnaire, NodeStack stack, List<Element> parents) {
    List<Element> list = getItems(element);
    for (int i = 0; i < list.size(); i++) {
      Element e = list.get(i);
      NodeStack ns = stack.push(e, i, e.getProperty().getDefinition(), e.getProperty().getDefinition());
      validateQuestionnaireElement(errors, ns, questionnaire, e, parents);
      List<Element> np = new ArrayList<Element>();
      np.add(e);
      np.addAll(parents);
      validateQuestionannaireItem(errors, e, questionnaire, ns, np);
    }
  }

  private void validateQuestionnaireElement(List<ValidationMessage> errors, NodeStack ns, Element questionnaire, Element item, List<Element> parents) {
    // R4+
    if ((FHIRVersion.isR4Plus(context.getVersion())) && (item.hasChildren("enableWhen"))) {
      List<Element> ewl = item.getChildren("enableWhen");
      for (Element ew : ewl) {
        String ql = ew.getNamedChildValue("question");
        if (rule(errors, IssueType.BUSINESSRULE, ns.literalPath, ql != null, I18nConstants.QUESTIONNAIRE_Q_ENABLEWHEN_NOLINK)) {
          Element tgt = getQuestionById(item, ql);
          if (rule(errors, IssueType.BUSINESSRULE, ns.literalPath, tgt == null, I18nConstants.QUESTIONNAIRE_Q_ENABLEWHEN_ISINNER)) {
            tgt = getQuestionById(questionnaire, ql);
            if (rule(errors, IssueType.BUSINESSRULE, ns.literalPath, tgt != null, I18nConstants.QUESTIONNAIRE_Q_ENABLEWHEN_NOTARGET, ql)) {
              if (rule(errors, IssueType.BUSINESSRULE, ns.literalPath, tgt != item, I18nConstants.QUESTIONNAIRE_Q_ENABLEWHEN_SELF)) {
                if (!isBefore(item, tgt, parents)) {
                  warning(errors, IssueType.BUSINESSRULE, ns.literalPath, false, I18nConstants.QUESTIONNAIRE_Q_ENABLEWHEN_AFTER, ql);
                }
              }
            }
          }
        }
      }
    }
  }

  private boolean isBefore(Element item, Element tgt, List<Element> parents) {
    // we work up the list, looking for tgt in the children of the parents
    if (parents.contains(tgt)) {
      // actually, if the target is a parent, that's automatically ok
      return true;
    }
    for (Element p : parents) {
      int i = findIndex(p, item);
      int t = findIndex(p, tgt);
      if (i > -1 && t > -1) {
        return i > t;
      }
    }
    return false; // unsure... shouldn't ever get to this point;
  }


  private int findIndex(Element parent, Element descendant) {
    for (int i = 0; i < parent.getChildren().size(); i++) {
      if (parent.getChildren().get(i) == descendant || isChild(parent.getChildren().get(i), descendant))
        return i;
    }
    return -1;
  }

  private boolean isChild(Element element, Element descendant) {
    for (Element e : element.getChildren()) {
      if (e == descendant)
        return true;
      if (isChild(e, descendant))
        return true;
    }
    return false;
  }

  private Element getQuestionById(Element focus, String ql) {
    List<Element> list = getItems(focus);
    for (Element item : list) {
      String v = item.getNamedChildValue("linkId");
      if (ql.equals(v))
        return item;
      Element tgt = getQuestionById(item, ql);
      if (tgt != null)
        return tgt;
    }
    return null;

  }

  private List<Element> getItems(Element element) {
    List<Element> list = new ArrayList<>();
    element.getNamedChildren("item", list);
    return list;
  }

  private void checkLang(Element resource, NodeStack stack) {
    String lang = resource.getNamedChildValue("language");
    if (!Utilities.noString(lang))
      stack.workingLang = lang;
  }

  private void validateResourceRules(List<ValidationMessage> errors, Element element, NodeStack stack) {
    String lang = element.getNamedChildValue("language");
    Element text = element.getNamedChild("text");
    if (text != null) {
      Element div = text.getNamedChild("div");
      if (lang != null && div != null) {
        XhtmlNode xhtml = div.getXhtml();
        String l = xhtml.getAttribute("lang");
        String xl = xhtml.getAttribute("xml:lang");
        if (l == null && xl == null) {
          warning(errors, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, I18nConstants.LANGUAGE_XHTML_LANG_MISSING1);
        } else {
          if (l == null) {
            warning(errors, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, I18nConstants.LANGUAGE_XHTML_LANG_MISSING2);
          } else if (!l.equals(lang)) {
            warning(errors, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, I18nConstants.LANGUAGE_XHTML_LANG_DIFFERENT1, lang, l);
          }
          if (xl == null) {
            warning(errors, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, I18nConstants.LANGUAGE_XHTML_LANG_MISSING3);
          } else if (!xl.equals(lang)) {
            warning(errors, IssueType.BUSINESSRULE, div.line(), div.col(), stack.getLiteralPath(), false, I18nConstants.LANGUAGE_XHTML_LANG_DIFFERENT2, lang, xl);
          }
        }
      }
    }
    // security tags are a set (system|code)
    Element meta = element.getNamedChild("meta");
    if (meta != null) {
      Set<String> tags = new HashSet<>();
      List<Element> list = new ArrayList<>();
      meta.getNamedChildren("security", list);
      int i = 0;
      for (Element e : list) {
        String s = e.getNamedChildValue("system") + "#" + e.getNamedChildValue("code");
        rule(errors, IssueType.BUSINESSRULE, e.line(), e.col(), stack.getLiteralPath() + ".meta.profile[" + Integer.toString(i) + "]", !tags.contains(s), I18nConstants.META_RES_SECURITY_DUPLICATE, s);
        tags.add(s);
        i++;
      }
    }
  }

  private void validateCapabilityStatement(List<ValidationMessage> errors, Element cs, NodeStack stack) {
    int iRest = 0;
    for (Element rest : cs.getChildrenByName("rest")) {
      int iResource = 0;
      for (Element resource : rest.getChildrenByName("resource")) {
        int iSP = 0;
        for (Element searchParam : resource.getChildrenByName("searchParam")) {
          String ref = searchParam.getChildValue("definition");
          String type = searchParam.getChildValue("type");
          if (!Utilities.noString(ref)) {
            SearchParameter sp = context.fetchResource(SearchParameter.class, ref);
            if (sp != null) {
              rule(errors, IssueType.INVALID, searchParam.line(), searchParam.col(), stack.literalPath + ".rest[" + iRest + "].resource[" + iResource + "].searchParam[" + iSP + "]",
                sp.getType().toCode().equals(type), I18nConstants.CAPABALITYSTATEMENT_CS_SP_WRONGTYPE, sp.getUrl(), sp.getType().toCode(), type);
            }
          }
          iSP++;
        }
        iResource++;
      }
      iRest++;
    }
  }

  private void validateCodeSystem(List<ValidationMessage> errors, Element cs, NodeStack stack) {
    String url = cs.getNamedChildValue("url");
    String vsu = cs.getNamedChildValue("valueSet");
    if (!Utilities.noString(vsu)) {
      ValueSet vs;
      try {
        vs = context.fetchResourceWithException(ValueSet.class, vsu);
      } catch (FHIRException e) {
        vs = null;
      }
      if (vs != null) {
        if (rule(errors, IssueType.BUSINESSRULE, stack.getLiteralPath(), vs.hasCompose() && !vs.hasExpansion(), I18nConstants.CODESYSTEM_CS_VS_MISMATCH, url, vsu))
          if (rule(errors, IssueType.BUSINESSRULE, stack.getLiteralPath(), vs.getCompose().getInclude().size() == 1, I18nConstants.CODESYSTEM_CS_VS_INVALID, url, vsu))
            if (rule(errors, IssueType.BUSINESSRULE, stack.getLiteralPath(), vs.getCompose().getInclude().get(0).getSystem().equals(url), I18nConstants.CODESYSTEM_CS_VS_WRONGSYSTEM, url, vsu, vs.getCompose().getInclude().get(0).getSystem())) {
              rule(errors, IssueType.BUSINESSRULE, stack.getLiteralPath(), !vs.getCompose().getInclude().get(0).hasValueSet()
                && !vs.getCompose().getInclude().get(0).hasConcept() && !vs.getCompose().getInclude().get(0).hasFilter(), I18nConstants.CODESYSTEM_CS_VS_INCLUDEDETAILS, url, vsu);
            }
      }
    } // todo... try getting the value set the other way...
  }

  private void validateQuestionannaireResponse(ValidatorHostContext hostContext, List<ValidationMessage> errors, Element element, NodeStack stack) throws FHIRException {
    Element q = element.getNamedChild("questionnaire");
    String questionnaire = null;
    if (q != null) {
      /*
       * q.getValue() is correct for R4 content, but we'll also accept the second
       * option just in case we're validating raw STU3 content. Being lenient here
       * isn't the end of the world since if someone is actually doing the reference
       * wrong in R4 content it'll get flagged elsewhere by the validator too
       */
      if (isNotBlank(q.getValue())) {
        questionnaire = q.getValue();
      } else if (isNotBlank(q.getChildValue("reference"))) {
        questionnaire = q.getChildValue("reference");
      }
    }
    if (hint(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), questionnaire != null, I18nConstants.QUESTIONNAIRE_QR_Q_NONE)) {
      long t = System.nanoTime();
      Questionnaire qsrc = questionnaire.startsWith("#") ? loadQuestionnaire(element, questionnaire.substring(1)) : context.fetchResource(Questionnaire.class, questionnaire);
      sdTime = sdTime + (System.nanoTime() - t);
      if (warning(errors, IssueType.REQUIRED, q.line(), q.col(), stack.getLiteralPath(), qsrc != null, I18nConstants.QUESTIONNAIRE_QR_Q_NOTFOUND, questionnaire)) {
        boolean inProgress = "in-progress".equals(element.getNamedChildValue("status"));
        validateQuestionannaireResponseItems(hostContext, qsrc, qsrc.getItem(), errors, element, stack, inProgress, element, new QStack(qsrc, element));
      }
    }
  }

  private Questionnaire loadQuestionnaire(Element resource, String id) throws FHIRException {
    try {
      for (Element contained : resource.getChildren("contained")) {
        if (contained.getIdBase().equals(id)) {
          FhirPublication v = FhirPublication.fromCode(context.getVersion());
          ByteArrayOutputStream bs = new ByteArrayOutputStream();
          new JsonParser(context).compose(contained, bs, OutputStyle.NORMAL, id);
          byte[] json = bs.toByteArray();
          switch (v) {
            case DSTU1:
              throw new FHIRException(context.formatMessage(I18nConstants.UNSUPPORTED_VERSION_R1));
            case DSTU2:
              org.hl7.fhir.dstu2.model.Resource r2 = new org.hl7.fhir.dstu2.formats.JsonParser().parse(json);
              Resource r5 = VersionConvertor_10_50.convertResource(r2);
              if (r5 instanceof Questionnaire)
                return (Questionnaire) r5;
              else
                return null;
            case DSTU2016May:
              org.hl7.fhir.dstu2016may.model.Resource r2a = new org.hl7.fhir.dstu2016may.formats.JsonParser().parse(json);
              r5 = VersionConvertor_14_50.convertResource(r2a);
              if (r5 instanceof Questionnaire)
                return (Questionnaire) r5;
              else
                return null;
            case STU3:
              org.hl7.fhir.dstu3.model.Resource r3 = new org.hl7.fhir.dstu3.formats.JsonParser().parse(json);
              r5 = VersionConvertor_30_50.convertResource(r3, false);
              if (r5 instanceof Questionnaire)
                return (Questionnaire) r5;
              else
                return null;
            case R4:
              org.hl7.fhir.r4.model.Resource r4 = new org.hl7.fhir.r4.formats.JsonParser().parse(json);
              r5 = VersionConvertor_40_50.convertResource(r4);
              if (r5 instanceof Questionnaire)
                return (Questionnaire) r5;
              else
                return null;
            case R5:
              r5 = new org.hl7.fhir.r5.formats.JsonParser().parse(json);
              if (r5 instanceof Questionnaire)
                return (Questionnaire) r5;
              else
                return null;
          }
        }
      }
      return null;
    } catch (IOException e) {
      throw new FHIRException(e);
    }
  }

  private void validateQuestionnaireResponseItem(ValidatorHostContext hostContext, Questionnaire qsrc, QuestionnaireItemComponent qItem, List<ValidationMessage> errors, Element element, NodeStack stack, boolean inProgress, Element questionnaireResponseRoot, QStack qstack) {
    String text = element.getNamedChildValue("text");
    rule(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), Utilities.noString(text) || text.equals(qItem.getText()), I18nConstants.QUESTIONNAIRE_QR_ITEM_TEXT, qItem.getLinkId());

    List<Element> answers = new ArrayList<Element>();
    element.getNamedChildren("answer", answers);
    if (inProgress)
      warning(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), isAnswerRequirementFulfilled(qItem, answers), I18nConstants.QUESTIONNAIRE_QR_ITEM_MISSING, qItem.getLinkId());
    else if (myEnableWhenEvaluator.isQuestionEnabled(hostContext, qItem, qstack, fpe)) {
      rule(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), isAnswerRequirementFulfilled(qItem, answers), I18nConstants.QUESTIONNAIRE_QR_ITEM_MISSING, qItem.getLinkId());
    } else if (!answers.isEmpty()) { // items without answers should be allowed, but not items with answers to questions that are disabled
      // it appears that this is always a duplicate error - it will always already have beeb reported, so no need to report it again?
      // GDG 2019-07-13
//      rule(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), !isAnswerRequirementFulfilled(qItem, answers), I18nConstants.QUESTIONNAIRE_QR_ITEM_NOTENABLED, qItem.getLinkId());
    }

    if (answers.size() > 1)
      rule(errors, IssueType.INVALID, answers.get(1).line(), answers.get(1).col(), stack.getLiteralPath(), qItem.getRepeats(), I18nConstants.QUESTIONNAIRE_QR_ITEM_ONLYONEA);

    for (Element answer : answers) {
      NodeStack ns = stack.push(answer, -1, null, null);
      if (qItem.getType() != null) {
        switch (qItem.getType()) {
          case GROUP:
            rule(errors, IssueType.STRUCTURE, answer.line(), answer.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_GROUP);
            break;
          case DISPLAY:  // nothing
            break;
          case BOOLEAN:
            validateQuestionnaireResponseItemType(errors, answer, ns, "boolean");
            break;
          case DECIMAL:
            validateQuestionnaireResponseItemType(errors, answer, ns, "decimal");
            break;
          case INTEGER:
            validateQuestionnaireResponseItemType(errors, answer, ns, "integer");
            break;
          case DATE:
            validateQuestionnaireResponseItemType(errors, answer, ns, "date");
            break;
          case DATETIME:
            validateQuestionnaireResponseItemType(errors, answer, ns, "dateTime");
            break;
          case TIME:
            validateQuestionnaireResponseItemType(errors, answer, ns, "time");
            break;
          case STRING:
            validateQuestionnaireResponseItemType(errors, answer, ns, "string");
            break;
          case TEXT:
            validateQuestionnaireResponseItemType(errors, answer, ns, "text");
            break;
          case URL:
            validateQuestionnaireResponseItemType(errors, answer, ns, "uri");
            break;
          case ATTACHMENT:
            validateQuestionnaireResponseItemType(errors, answer, ns, "Attachment");
            break;
          case REFERENCE:
            validateQuestionnaireResponseItemType(errors, answer, ns, "Reference");
            break;
          case QUANTITY:
            if ("Quantity".equals(validateQuestionnaireResponseItemType(errors, answer, ns, "Quantity")))
              if (qItem.hasExtension("???"))
                validateQuestionnaireResponseItemQuantity(errors, answer, ns);
            break;
          case CHOICE:
            String itemType = validateQuestionnaireResponseItemType(errors, answer, ns, "Coding", "date", "time", "integer", "string");
            if (itemType != null) {
              if (itemType.equals("Coding")) validateAnswerCode(errors, answer, ns, qsrc, qItem, false);
              else if (itemType.equals("date")) checkOption(errors, answer, ns, qsrc, qItem, "date");
              else if (itemType.equals("time")) checkOption(errors, answer, ns, qsrc, qItem, "time");
              else if (itemType.equals("integer"))
                checkOption(errors, answer, ns, qsrc, qItem, "integer");
              else if (itemType.equals("string")) checkOption(errors, answer, ns, qsrc, qItem, "string");
            }
            break;
          case OPENCHOICE:
            itemType = validateQuestionnaireResponseItemType(errors, answer, ns, "Coding", "date", "time", "integer", "string");
            if (itemType != null) {
              if (itemType.equals("Coding")) validateAnswerCode(errors, answer, ns, qsrc, qItem, true);
              else if (itemType.equals("date")) checkOption(errors, answer, ns, qsrc, qItem, "date");
              else if (itemType.equals("time")) checkOption(errors, answer, ns, qsrc, qItem, "time");
              else if (itemType.equals("integer"))
                checkOption(errors, answer, ns, qsrc, qItem, "integer");
              else if (itemType.equals("string"))
                checkOption(errors, answer, ns, qsrc, qItem, "string", true);
            }
            break;
//          case QUESTION:
          case NULL:
            // no validation
            break;
        }
      }
      validateQuestionannaireResponseItems(hostContext, qsrc, qItem.getItem(), errors, answer, stack, inProgress, questionnaireResponseRoot, qstack);
    }
    if (qItem.getType() == null) {
      fail(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOTYPE, qItem.getLinkId());
    } else if (qItem.getType() == QuestionnaireItemType.DISPLAY) {
      List<Element> items = new ArrayList<Element>();
      element.getNamedChildren("item", items);
      rule(errors, IssueType.STRUCTURE, element.line(), element.col(), stack.getLiteralPath(), items.isEmpty(), I18nConstants.QUESTIONNAIRE_QR_ITEM_DISPLAY, qItem.getLinkId());
    } else {
      validateQuestionannaireResponseItems(hostContext, qsrc, qItem.getItem(), errors, element, stack, inProgress, questionnaireResponseRoot, qstack);
    }
  }

  private boolean isAnswerRequirementFulfilled(QuestionnaireItemComponent qItem, List<Element> answers) {
    return !answers.isEmpty() || !qItem.getRequired() || qItem.getType() == QuestionnaireItemType.GROUP;
  }

  private void validateQuestionnaireResponseItem(ValidatorHostContext hostcontext, Questionnaire qsrc, QuestionnaireItemComponent qItem, List<ValidationMessage> errors, List<Element> elements, NodeStack stack, boolean inProgress, Element questionnaireResponseRoot, QStack qstack) {
    if (elements.size() > 1)
      rule(errors, IssueType.INVALID, elements.get(1).line(), elements.get(1).col(), stack.getLiteralPath(), qItem.getRepeats(), I18nConstants.QUESTIONNAIRE_QR_ITEM_ONLYONEI, qItem.getLinkId());
    int i = 0;
    for (Element element : elements) {
      NodeStack ns = stack.push(element, i, null, null);
      validateQuestionnaireResponseItem(hostcontext, qsrc, qItem, errors, element, ns, inProgress, questionnaireResponseRoot, qstack.push(qItem, element));
      i++;
    }
  }

  private int getLinkIdIndex(List<QuestionnaireItemComponent> qItems, String linkId) {
    for (int i = 0; i < qItems.size(); i++) {
      if (linkId.equals(qItems.get(i).getLinkId()))
        return i;
    }
    return -1;
  }

  private void validateQuestionannaireResponseItems(ValidatorHostContext hostContext, Questionnaire qsrc, List<QuestionnaireItemComponent> qItems, List<ValidationMessage> errors, Element element, NodeStack stack, boolean inProgress, Element questionnaireResponseRoot, QStack qstack) {
    List<Element> items = new ArrayList<Element>();
    element.getNamedChildren("item", items);
    // now, sort into stacks
    Map<String, List<Element>> map = new HashMap<String, List<Element>>();
    int lastIndex = -1;
    for (Element item : items) {
      String linkId = item.getNamedChildValue("linkId");
      if (rule(errors, IssueType.REQUIRED, item.line(), item.col(), stack.getLiteralPath(), !Utilities.noString(linkId), I18nConstants.QUESTIONNAIRE_QR_ITEM_NOLINKID)) {
        int index = getLinkIdIndex(qItems, linkId);
        if (index == -1) {
          QuestionnaireItemComponent qItem = findQuestionnaireItem(qsrc, linkId);
          if (qItem != null) {
            rule(errors, IssueType.STRUCTURE, item.line(), item.col(), stack.getLiteralPath(), index > -1, misplacedItemError(qItem));
            NodeStack ns = stack.push(item, -1, null, null);
            validateQuestionnaireResponseItem(hostContext, qsrc, qItem, errors, item, ns, inProgress, questionnaireResponseRoot, qstack.push(qItem, item));
          } else
            rule(errors, IssueType.NOTFOUND, item.line(), item.col(), stack.getLiteralPath(), index > -1, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOTFOUND, linkId);
        } else {
          rule(errors, IssueType.STRUCTURE, item.line(), item.col(), stack.getLiteralPath(), index >= lastIndex, I18nConstants.QUESTIONNAIRE_QR_ITEM_ORDER);
          lastIndex = index;

          // If an item has a child called "linkId" but no child called "answer",
          // we'll treat it as not existing for the purposes of enableWhen validation
          if (item.hasChildren("answer") || item.hasChildren("item")) {
            List<Element> mapItem = map.computeIfAbsent(linkId, key -> new ArrayList<>());
            mapItem.add(item);
          }
        }
      }
    }

    // ok, now we have a list of known items, grouped by linkId. We've made an error for anything out of order
    for (QuestionnaireItemComponent qItem : qItems) {
      List<Element> mapItem = map.get(qItem.getLinkId());
      validateQuestionnaireResponseItem(hostContext, qsrc, errors, element, stack, inProgress, questionnaireResponseRoot, qItem, mapItem, qstack);
    }
  }

  public void validateQuestionnaireResponseItem(ValidatorHostContext hostContext, Questionnaire qsrc, List<ValidationMessage> errors, Element element, NodeStack stack, boolean inProgress, Element questionnaireResponseRoot, QuestionnaireItemComponent qItem, List<Element> mapItem, QStack qstack) {
    boolean enabled = myEnableWhenEvaluator.isQuestionEnabled(hostContext, qItem, qstack, fpe);
    if (mapItem != null) {
      if (!enabled) {
        int i = 0;
        for (Element e : mapItem) {
          NodeStack ns = stack.push(e, i, e.getProperty().getDefinition(), e.getProperty().getDefinition());
          rule(errors, IssueType.INVALID, e.line(), e.col(), ns.getLiteralPath(), enabled, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOTENABLED2, qItem.getLinkId());
          i++;
        }
      }

      // Recursively validate child items
      validateQuestionnaireResponseItem(hostContext, qsrc, qItem, errors, mapItem, stack, inProgress, questionnaireResponseRoot, qstack);

    } else {

      // item is missing, is the question enabled?
      if (enabled && qItem.getRequired()) {
        String message = "No response found for required item with id = '" + qItem.getLinkId() + "'";
        if (inProgress) {
          warning(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), false, message);
        } else {
          rule(errors, IssueType.REQUIRED, element.line(), element.col(), stack.getLiteralPath(), false, message);
        }
      }

    }

  }

  private String misplacedItemError(QuestionnaireItemComponent qItem) {
    return qItem.hasLinkId() ? String.format("Structural Error: item with linkid %s is in the wrong place", qItem.getLinkId()) : "Structural Error: item is in the wrong place";
  }

  private void validateQuestionnaireResponseItemQuantity(List<ValidationMessage> errors, Element answer, NodeStack stack) {

  }

  private String validateQuestionnaireResponseItemType(List<ValidationMessage> errors, Element element, NodeStack stack, String... types) {
    List<Element> values = new ArrayList<Element>();
    element.getNamedChildrenWithWildcard("value[x]", values);
    for (int i = 0; i < types.length; i++) {
      if (types[i].equals("text")) {
        types[i] = "string";
      }
    }
    if (values.size() > 0) {
      NodeStack ns = stack.push(values.get(0), -1, null, null);
      CommaSeparatedStringBuilder l = new CommaSeparatedStringBuilder();
      for (String s : types) {
        l.append(s);
        if (values.get(0).getName().equals("value" + Utilities.capitalize(s)))
          return (s);
      }
      if (types.length == 1)
        rule(errors, IssueType.STRUCTURE, values.get(0).line(), values.get(0).col(), ns.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_WRONGTYPE, types[0]);
      else
        rule(errors, IssueType.STRUCTURE, values.get(0).line(), values.get(0).col(), ns.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_WRONGTYPE2, l.toString());
    }
    return null;
  }

  private QuestionnaireItemComponent findQuestionnaireItem(Questionnaire qSrc, String linkId) {
    return findItem(qSrc.getItem(), linkId);
  }

  private QuestionnaireItemComponent findItem(List<QuestionnaireItemComponent> list, String linkId) {
    for (QuestionnaireItemComponent item : list) {
      if (linkId.equals(item.getLinkId()))
        return item;
      QuestionnaireItemComponent result = findItem(item.getItem(), linkId);
      if (result != null)
        return result;
    }
    return null;
  }

  private void validateAnswerCode(List<ValidationMessage> errors, Element value, NodeStack stack, Questionnaire qSrc, String ref, boolean theOpenChoice) {
    ValueSet vs = resolveBindingReference(qSrc, ref, qSrc.getUrl());
    if (warning(errors, IssueType.CODEINVALID, value.line(), value.col(), stack.getLiteralPath(), vs != null, I18nConstants.TERMINOLOGY_TX_VALUESET_NOTFOUND, describeReference(ref))) {
      try {
        Coding c = ObjectConverter.readAsCoding(value);
        if (isBlank(c.getCode()) && isBlank(c.getSystem()) && isNotBlank(c.getDisplay())) {
          if (theOpenChoice) {
            return;
          }
        }

        long t = System.nanoTime();
        ValidationResult res = context.validateCode(new ValidationOptions(stack.workingLang), c, vs);
        txTime = txTime + (System.nanoTime() - t);
        if (!res.isOk()) {
          txRule(errors, res.getTxLink(), IssueType.CODEINVALID, value.line(), value.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_BADOPTION, c.getSystem(), c.getCode());
        } else if (res.getSeverity() != null) {
          super.addValidationMessage(errors, IssueType.CODEINVALID, value.line(), value.col(), stack.getLiteralPath(), res.getMessage(), res.getSeverity(), Source.TerminologyEngine);
        }
      } catch (Exception e) {
        warning(errors, IssueType.CODEINVALID, value.line(), value.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_CODING, e.getMessage());
      }
    }
  }

  private void validateAnswerCode(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean theOpenChoice) {
    Element v = answer.getNamedChild("valueCoding");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0)
      checkCodingOption(errors, answer, stack, qSrc, qItem, theOpenChoice);
      //	    validateAnswerCode(errors, v, stack, qItem.getOption());
    else if (qItem.hasAnswerValueSet())
      validateAnswerCode(errors, v, stack, qSrc, qItem.getAnswerValueSet(), theOpenChoice);
    else
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONS);
  }

  private void checkOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, String type) {
    checkOption(errors, answer, stack, qSrc, qItem, type, false);
  }

  private void checkOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, String type, boolean openChoice) {
    if (type.equals("integer")) checkIntegerOption(errors, answer, stack, qSrc, qItem, openChoice);
    else if (type.equals("date")) checkDateOption(errors, answer, stack, qSrc, qItem, openChoice);
    else if (type.equals("time")) checkTimeOption(errors, answer, stack, qSrc, qItem, openChoice);
    else if (type.equals("string")) checkStringOption(errors, answer, stack, qSrc, qItem, openChoice);
    else if (type.equals("Coding")) checkCodingOption(errors, answer, stack, qSrc, qItem, openChoice);
  }

  private void checkIntegerOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean openChoice) {
    Element v = answer.getNamedChild("valueInteger");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0) {
      List<IntegerType> list = new ArrayList<IntegerType>();
      for (QuestionnaireItemAnswerOptionComponent components : qItem.getAnswerOption()) {
        try {
          list.add(components.getValueIntegerType());
        } catch (FHIRException e) {
          // If it's the wrong type, just keep going
        }
      }
      if (list.isEmpty() && !openChoice) {
        rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONSINTEGER);
      } else {
        boolean found = false;
        for (IntegerType item : list) {
          if (item.getValue() == Integer.parseInt(v.primitiveValue())) {
            found = true;
            break;
          }
        }
        if (!found) {
          rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), found, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOINTEGER, v.primitiveValue());
        }
      }
    } else
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_INTNOOPTIONS);
  }

  private void checkDateOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean openChoice) {
    Element v = answer.getNamedChild("valueDate");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0) {
      List<DateType> list = new ArrayList<DateType>();
      for (QuestionnaireItemAnswerOptionComponent components : qItem.getAnswerOption()) {
        try {
          list.add(components.getValueDateType());
        } catch (FHIRException e) {
          // If it's the wrong type, just keep going
        }
      }
      if (list.isEmpty() && !openChoice) {
        rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONSDATE);
      } else {
        boolean found = false;
        for (DateType item : list) {
          if (item.getValue().equals(v.primitiveValue())) {
            found = true;
            break;
          }
        }
        if (!found) {
          rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), found, I18nConstants.QUESTIONNAIRE_QR_ITEM_NODATE, v.primitiveValue());
        }
      }
    } else
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_DATENOOPTIONS);
  }

  private void checkTimeOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean openChoice) {
    Element v = answer.getNamedChild("valueTime");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0) {
      List<TimeType> list = new ArrayList<TimeType>();
      for (QuestionnaireItemAnswerOptionComponent components : qItem.getAnswerOption()) {
        try {
          list.add(components.getValueTimeType());
        } catch (FHIRException e) {
          // If it's the wrong type, just keep going
        }
      }
      if (list.isEmpty() && !openChoice) {
        rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONSTIME);
      } else {
        boolean found = false;
        for (TimeType item : list) {
          if (item.getValue().equals(v.primitiveValue())) {
            found = true;
            break;
          }
        }
        if (!found) {
          rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), found, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOTIME, v.primitiveValue());
        }
      }
    } else
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_TIMENOOPTIONS);
  }

  private void checkStringOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean openChoice) {
    Element v = answer.getNamedChild("valueString");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0) {
      List<StringType> list = new ArrayList<StringType>();
      for (QuestionnaireItemAnswerOptionComponent components : qItem.getAnswerOption()) {
        try {
          if (components.getValue() != null) {
            list.add(components.getValueStringType());
          }
        } catch (FHIRException e) {
          // If it's the wrong type, just keep going
        }
      }
      if (!openChoice) {
        if (list.isEmpty()) {
          rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONSSTRING);
        } else {
          boolean found = false;
          for (StringType item : list) {
            if (item.getValue().equals((v.primitiveValue()))) {
              found = true;
              break;
            }
          }
          if (!found) {
            rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), found, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOSTRING, v.primitiveValue());
          }
        }
      }
    } else {
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_STRINGNOOPTIONS);
    }
  }

  private void checkCodingOption(List<ValidationMessage> errors, Element answer, NodeStack stack, Questionnaire qSrc, QuestionnaireItemComponent qItem, boolean openChoice) {
    Element v = answer.getNamedChild("valueCoding");
    String system = v.getNamedChildValue("system");
    String code = v.getNamedChildValue("code");
    NodeStack ns = stack.push(v, -1, null, null);
    if (qItem.getAnswerOption().size() > 0) {
      List<Coding> list = new ArrayList<Coding>();
      for (QuestionnaireItemAnswerOptionComponent components : qItem.getAnswerOption()) {
        try {
          if (components.getValue() != null) {
            list.add(components.getValueCoding());
          }
        } catch (FHIRException e) {
          // If it's the wrong type, just keep going
        }
      }
      if (list.isEmpty() && !openChoice) {
        rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOOPTIONSCODING);
      } else {
        boolean found = false;
        for (Coding item : list) {
          if (ObjectUtil.equals(item.getSystem(), system) && ObjectUtil.equals(item.getCode(), code)) {
            found = true;
            break;
          }
        }
        if (!found) {
          rule(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), found, I18nConstants.QUESTIONNAIRE_QR_ITEM_NOCODING, system, code);
        }
      }
    } else
      hint(errors, IssueType.STRUCTURE, v.line(), v.col(), stack.getLiteralPath(), false, I18nConstants.QUESTIONNAIRE_QR_ITEM_CODINGNOOPTIONS);
  }

  private String tail(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }

  private String tryParse(String ref) {
    String[] parts = ref.split("\\/");
    switch (parts.length) {
      case 1:
        return null;
      case 2:
        return checkResourceType(parts[0]);
      default:
        if (parts[parts.length - 2].equals("_history"))
          return checkResourceType(parts[parts.length - 4]);
        else
          return checkResourceType(parts[parts.length - 2]);
    }
  }

  private boolean typesAreAllReference(List<TypeRefComponent> theType) {
    for (TypeRefComponent typeRefComponent : theType) {
      if (typeRefComponent.getCode().equals("Reference") == false) {
        return false;
      }
    }
    return true;
  }

  private void validateBundle(List<ValidationMessage> errors, Element bundle, NodeStack stack, boolean checkSpecials) {
    List<Element> entries = new ArrayList<Element>();
    bundle.getNamedChildren("entry", entries);
    String type = bundle.getNamedChildValue("type");
    type = StringUtils.defaultString(type);

    if (entries.size() == 0) {
      rule(errors, IssueType.INVALID, stack.getLiteralPath(), !(type.equals("document") || type.equals("message")), I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRST);
    } else {
      // Get the first entry, the MessageHeader
      Element firstEntry = entries.get(0);
      // Get the stack of the first entry
      NodeStack firstStack = stack.push(firstEntry, 1, null, null);

      String fullUrl = firstEntry.getNamedChildValue("fullUrl");

      if (type.equals("document")) {
        Element resource = firstEntry.getNamedChild("resource");
        String id = resource.getNamedChildValue("id");
        if (rule(errors, IssueType.INVALID, firstEntry.line(), firstEntry.col(), stack.addToLiteralPath("entry", ":0"), resource != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRSTRESOURCE)) {
          validateDocument(errors, entries, resource, firstStack.push(resource, -1, null, null), fullUrl, id);
        }
        checkAllInterlinked(errors, entries, stack, bundle, true);
      }
      if (type.equals("message")) {
        Element resource = firstEntry.getNamedChild("resource");
        String id = resource.getNamedChildValue("id");
        if (rule(errors, IssueType.INVALID, firstEntry.line(), firstEntry.col(), stack.addToLiteralPath("entry", ":0"), resource != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOFIRSTRESOURCE)) {
          validateMessage(errors, entries, resource, firstStack.push(resource, -1, null, null), fullUrl, id);
        }
        checkAllInterlinked(errors, entries, stack, bundle, VersionUtilities.isR5Ver(context.getVersion()));
      }
      // We do not yet have rules requiring that the id and fullUrl match when dealing with messaging Bundles
      //      validateResourceIds(errors, entries, stack);
    }
    for (Element entry : entries) {
      String fullUrl = entry.getNamedChildValue("fullUrl");
      String url = getCanonicalURLForEntry(entry);
      String id = getIdForEntry(entry);
      if (url != null) {
        if (!(!url.equals(fullUrl) || (url.matches(uriRegexForVersion()) && url.endsWith("/" + id))) && !isV3orV2Url(url))
          rule(errors, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath("entry", ":0"), false, I18nConstants.BUNDLE_BUNDLE_ENTRY_MISMATCHIDURL, url, fullUrl, id);
        rule(errors, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath("entry", ":0"), !url.equals(fullUrl) || serverBase == null || (url.equals(Utilities.pathURL(serverBase, entry.getNamedChild("resource").fhirType(), id))), I18nConstants.BUNDLE_BUNDLE_ENTRY_CANONICAL, url, fullUrl);
      }
      // todo: check specials
    }
  }

  // hack for pre-UTG v2/v3
  private boolean isV3orV2Url(String url) {
    return url.startsWith("http://hl7.org/fhir/v3/") || url.startsWith("http://hl7.org/fhir/v2/");
  }

  public final static String URI_REGEX3 = "((http|https)://([A-Za-z0-9\\\\\\.\\:\\%\\$]*\\/)*)?(Account|ActivityDefinition|AllergyIntolerance|AdverseEvent|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BodySite|Bundle|CapabilityStatement|CarePlan|CareTeam|ChargeItem|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition (aka Problem)|Consent|Contract|Coverage|DataElement|DetectedIssue|Device|DeviceComponent|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EligibilityRequest|EligibilityResponse|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|ExpansionProfile|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingManifest|ImagingStudy|Immunization|ImmunizationRecommendation|ImplementationGuide|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationRequest|MedicationStatement|MessageDefinition|MessageHeader|NamingSystem|NutritionOrder|Observation|OperationDefinition|OperationOutcome|Organization|Parameters|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|ProcedureRequest|ProcessRequest|ProcessResponse|Provenance|Questionnaire|QuestionnaireResponse|ReferralRequest|RelatedPerson|RequestGroup|ResearchStudy|ResearchSubject|RiskAssessment|Schedule|SearchParameter|Sequence|ServiceDefinition|Slot|Specimen|StructureDefinition|StructureMap|Subscription|Substance|SupplyDelivery|SupplyRequest|Task|TestScript|TestReport|ValueSet|VisionPrescription)\\/[A-Za-z0-9\\-\\.]{1,64}(\\/_history\\/[A-Za-z0-9\\-\\.]{1,64})?";
  private static final String EXECUTED_CONSTRAINT_LIST = "validator.executed.invariant.list";
  private static final String EXECUTION_ID = "validator.execution.id";

  private String uriRegexForVersion() {
    if (VersionUtilities.isR3Ver(context.getVersion()))
      return URI_REGEX3;
    else
      return Constants.URI_REGEX;
  }

  private String getCanonicalURLForEntry(Element entry) {
    Element e = entry.getNamedChild("resource");
    if (e == null)
      return null;
    return e.getNamedChildValue("url");
  }

  private String getIdForEntry(Element entry) {
    Element e = entry.getNamedChild("resource");
    if (e == null)
      return null;
    return e.getNamedChildValue("id");
  }

  /**
   * Check each resource entry to ensure that the entry's fullURL includes the resource's id
   * value. Adds an ERROR ValidationMessge to errors List for a given entry if it references
   * a resource and fullURL does not include the resource's id.
   *
   * @param errors  List of ValidationMessage objects that new errors will be added to.
   * @param entries List of entry Element objects to be checked.
   * @param stack   Current NodeStack used to create path names in error detail messages.
   */
  private void validateResourceIds(List<ValidationMessage> errors, List<Element> entries, NodeStack stack) {
    // TODO: Need to handle _version
    int i = 1;
    for (Element entry : entries) {
      String fullUrl = entry.getNamedChildValue("fullUrl");
      Element resource = entry.getNamedChild("resource");
      String id = resource != null ? resource.getNamedChildValue("id") : null;
      if (id != null && fullUrl != null) {
        String urlId = null;
        if (fullUrl.startsWith("https://") || fullUrl.startsWith("http://")) {
          urlId = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);
        } else if (fullUrl.startsWith("urn:uuid") || fullUrl.startsWith("urn:oid")) {
          urlId = fullUrl.substring(fullUrl.lastIndexOf(':') + 1);
        }
        rule(errors, IssueType.INVALID, entry.line(), entry.col(), stack.addToLiteralPath("entry[" + i + "]"), urlId.equals(id), I18nConstants.BUNDLE_BUNDLE_ENTRY_IDURLMISMATCH, id, fullUrl);
      }
      i++;
    }
  }

  private void checkAllInterlinked(List<ValidationMessage> errors, List<Element> entries, NodeStack stack, Element bundle, boolean isError) {
    List<EntrySummary> entryList = new ArrayList<>();
    for (Element entry : entries) {
      Element r = entry.getNamedChild("resource");
      if (r != null) {
        entryList.add(new EntrySummary(entry, r));
      }
    }
    for (EntrySummary e : entryList) {
      Set<String> references = findReferences(e.getEntry());
      for (String ref : references) {
        Element tgt = resolveInBundle(entries, ref, e.getEntry().getChildValue("fullUrl"), e.getResource().fhirType(), e.getResource().getIdBase());
        if (tgt != null) {
          EntrySummary t = entryForTarget(entryList, tgt);
          if (t != null) {
            e.getTargets().add(t);
          }
        }
      }
    }

    Set<EntrySummary> visited = new HashSet<>();
    visitLinked(visited, entryList.get(0));
    boolean foundRevLinks;
    do {
      foundRevLinks = false;
      for (EntrySummary e : entryList) {
        if (!visited.contains(e)) {
          boolean add = false;
          for (EntrySummary t : e.getTargets()) {
            if (visited.contains(t)) {
              add = true;
            }
          }
          if (add) {
            foundRevLinks = true;
            visitLinked(visited, e);
          }
        }
      }
    } while (foundRevLinks);

    int i = 0;
    for (EntrySummary e : entryList) {
      Element entry = e.getEntry();
      if (isError) {
        rule(errors, IssueType.INFORMATIONAL, entry.line(), entry.col(), stack.addToLiteralPath("entry" + '[' + (i + 1) + ']'), visited.contains(e), I18nConstants.BUNDLE_BUNDLE_ENTRY_ORPHAN, (entry.getChildValue("fullUrl") != null ? "'" + entry.getChildValue("fullUrl") + "'" : ""));
      } else {
        warning(errors, IssueType.INFORMATIONAL, entry.line(), entry.col(), stack.addToLiteralPath("entry" + '[' + (i + 1) + ']'), visited.contains(e), I18nConstants.BUNDLE_BUNDLE_ENTRY_ORPHAN, (entry.getChildValue("fullUrl") != null ? "'" + entry.getChildValue("fullUrl") + "'" : ""));
      }
      i++;
    }
  }

  private EntrySummary entryForTarget(List<EntrySummary> entryList, Element tgt) {
    for (EntrySummary e : entryList) {
      if (e.getEntry() == tgt) {
        return e;
      }
    }
    return null;
  }

  private void visitLinked(Set<EntrySummary> visited, EntrySummary t) {
    if (!visited.contains(t)) {
      visited.add(t);
      for (EntrySummary e : t.getTargets()) {
        visitLinked(visited, e);
      }
    }
  }

  private void followResourceLinks(Element entry, Map<String, Element> visitedResources, Map<Element, Element> candidateEntries, List<Element> candidateResources, List<ValidationMessage> errors, NodeStack stack) {
    followResourceLinks(entry, visitedResources, candidateEntries, candidateResources, errors, stack, 0);
  }

  private void followResourceLinks(Element entry, Map<String, Element> visitedResources, Map<Element, Element> candidateEntries, List<Element> candidateResources, List<ValidationMessage> errors, NodeStack stack, int depth) {
    Element resource = entry.getNamedChild("resource");
    if (visitedResources.containsValue(resource))
      return;

    visitedResources.put(entry.getNamedChildValue("fullUrl"), resource);

    String type = null;
    Set<String> references = findReferences(resource);
    for (String reference : references) {
      // We don't want errors when just retrieving the element as they will be caught (with better path info) in subsequent processing
      IndexedElement r = getFromBundle(stack.getElement(), reference, entry.getChildValue("fullUrl"), new ArrayList<ValidationMessage>(), stack.addToLiteralPath("entry[" + candidateResources.indexOf(resource) + "]"), type, "transaction".equals(stack.getElement().getChildValue("type")));
      if (r != null && !visitedResources.containsValue(r.getMatch())) {
        followResourceLinks(candidateEntries.get(r.getMatch()), visitedResources, candidateEntries, candidateResources, errors, stack, depth + 1);
      }
    }
  }

  private Set<String> findReferences(Element start) {
    Set<String> references = new HashSet<String>();
    findReferences(start, references);
    return references;
  }

  private void findReferences(Element start, Set<String> references) {
    for (Element child : start.getChildren()) {
      if (child.getType().equals("Reference")) {
        String ref = child.getChildValue("reference");
        if (ref != null && !ref.startsWith("#"))
          references.add(ref);
      }
      if (child.getType().equals("url") || child.getType().equals("uri") || child.getType().equals("canonical")) {
        String ref = child.primitiveValue();
        if (ref != null && !ref.startsWith("#"))
          references.add(ref);
      }
      findReferences(child, references);
    }
  }

  private void validateBundleReference(List<ValidationMessage> errors, List<Element> entries, Element ref, String name, NodeStack stack, String fullUrl, String type, String id) {
    String reference = null;
    try {
      reference = ref.getNamedChildValue("reference");
    } catch (Error e) {

    }

    if (ref != null && !Utilities.noString(reference)) {
      Element target = resolveInBundle(entries, reference, fullUrl, type, id);
      rule(errors, IssueType.INVALID, ref.line(), ref.col(), stack.addToLiteralPath("reference"), target != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND, reference, name);
    }
  }

  private void validateContains(ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, ElementDefinition child, ElementDefinition context, Element resource, Element element, NodeStack stack, IdStatus idstatus) throws FHIRException {
    String resourceName = element.getType();
    TypeRefComponent trr = null;
    for (TypeRefComponent tr : child.getType()) {
      if (tr.getCode().equals("Resource")) {
        trr = tr;
        break;
      }
    }
    if (trr == null) {
      rule(errors, IssueType.INFORMATIONAL, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_BUNDLE_ENTRY_TYPE, resourceName);
    } else if (isValidResourceType(resourceName, trr)) {
      long t = System.nanoTime();
      StructureDefinition profile = this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + resourceName);
      sdTime = sdTime + (System.nanoTime() - t);
      // special case: resource wrapper is reset if we're crossing a bundle boundary, but not otherwise
      ValidatorHostContext hc = null;
      if (element.getSpecial() == SpecialElement.BUNDLE_ENTRY || element.getSpecial() == SpecialElement.BUNDLE_OUTCOME || element.getSpecial() == SpecialElement.PARAMETER) {
        resource = element;
        hc = hostContext.forEntry(element);
      } else {
        hc = hostContext.forContained(element);
      }
      trackUsage(profile, hostContext, element);
      if (rule(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), profile != null, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOPROFILE, resourceName)) {
        validateResource(hc, errors, resource, element, profile, idstatus, stack);
      }
    } else {
      List<String> types = new ArrayList<>();
      for (UriType u : trr.getProfile()) {
        StructureDefinition sd = this.context.fetchResource(StructureDefinition.class, u.getValue());
        if (sd != null && !types.contains(sd.getType())) {
          types.add(sd.getType());
        }
      }
      if (types.size() == 1) {
        rule(errors, IssueType.INFORMATIONAL, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_BUNDLE_ENTRY_TYPE2, resourceName, types.get(0));
      } else {
        rule(errors, IssueType.INFORMATIONAL, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.BUNDLE_BUNDLE_ENTRY_TYPE3, resourceName, types);
      }
    }
  }

  private boolean isValidResourceType(String type, TypeRefComponent def) {
    if (!def.hasProfile()) {
      return true;
    }
    List<StructureDefinition> list = new ArrayList<>();
    for (UriType u : def.getProfile()) {
      StructureDefinition sdt = context.fetchResource(StructureDefinition.class, u.getValue());
      if (sdt != null) {
        list.add(sdt);
      }
    }

    StructureDefinition sdt = context.fetchTypeDefinition(type);
    while (sdt != null) {
      if (def.getWorkingCode().equals("Resource")) {
        for (StructureDefinition sd : list) {
          if (sd.getUrl().equals(sdt.getUrl())) {
            return true;
          }
          if (sd.getType().equals(sdt.getType())) {
            return true;
          }
        }
      }
      sdt = context.fetchResource(StructureDefinition.class, sdt.getBaseDefinition());
    }
    return false;
  }

  private void validateDocument(List<ValidationMessage> errors, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id) {
    // first entry must be a composition
    if (rule(errors, IssueType.INVALID, composition.line(), composition.col(), stack.getLiteralPath(), composition.getType().equals("Composition"), I18nConstants.BUNDLE_BUNDLE_ENTRY_DOCUMENT)) {

      // the composition subject etc references must resolve in the bundle
      validateDocumentReference(errors, entries, composition, stack, fullUrl, id, false, "subject", "Composition");
      validateDocumentReference(errors, entries, composition, stack, fullUrl, id, true, "author", "Composition");
      validateDocumentReference(errors, entries, composition, stack, fullUrl, id, false, "encounter", "Composition");
      validateDocumentReference(errors, entries, composition, stack, fullUrl, id, false, "custodian", "Composition");
      validateDocumentSubReference(errors, entries, composition, stack, fullUrl, id, "Composition", "attester", false, "party");
      validateDocumentSubReference(errors, entries, composition, stack, fullUrl, id, "Composition", "event", true, "detail");

      validateSections(errors, entries, composition, stack, fullUrl, id);
    }
  }

  public void validateDocumentSubReference(List<ValidationMessage> errors, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id, String title, String parent, boolean repeats, String propName) {
    List<Element> list = new ArrayList<>();
    composition.getNamedChildren(parent, list);
    int i = 1;
    for (Element elem : list) {
      validateDocumentReference(errors, entries, elem, stack.push(elem, i, null, null), fullUrl, id, repeats, propName, title + "." + parent);
      i++;
    }
  }

  public void validateDocumentReference(List<ValidationMessage> errors, List<Element> entries, Element composition, NodeStack stack, String fullUrl, String id, boolean repeats, String propName, String title) {
    if (repeats) {
      List<Element> list = new ArrayList<>();
      composition.getNamedChildren(propName, list);
      int i = 1;
      for (Element elem : list) {
        validateBundleReference(errors, entries, elem, title + "." + propName, stack.push(elem, i, null, null), fullUrl, "Composition", id);
        i++;
      }

    } else {
      Element elem = composition.getNamedChild(propName);
      if (elem != null) {
        validateBundleReference(errors, entries, elem, title + "." + propName, stack.push(elem, -1, null, null), fullUrl, "Composition", id);
      }
    }
  }

  private void validateElement(ValidatorHostContext hostContext, List<ValidationMessage> errors, StructureDefinition profile, ElementDefinition definition, StructureDefinition cprofile, ElementDefinition context,
    Element resource, Element element, String actualType, NodeStack stack, boolean inCodeableConcept, boolean checkDisplayInContext, String extensionUrl) throws FHIRException {

    // check type invariants
    checkInvariants(hostContext, errors, profile, definition, resource, element, stack, false);
    if (definition.getFixed() != null)
      checkFixedValue(errors, stack.getLiteralPath(), element, definition.getFixed(), profile.getUrl(), definition.getSliceName(), null);

    // get the list of direct defined children, including slices
    List<ElementDefinition> childDefinitions = profileUtilities.getChildMap(profile, definition);
    if (childDefinitions.isEmpty()) {
      if (actualType == null)
        return; // there'll be an error elsewhere in this case, and we're going to stop.
      childDefinitions = getActualTypeChildren(hostContext, element, actualType);
    } else if (definition.getType().size() > 1) {
      // this only happens when the profile constrains the abstract children but leaves th choice open.
      if (actualType == null)
        return; // there'll be an error elsewhere in this case, and we're going to stop.
      List<ElementDefinition> typeChildDefinitions = getActualTypeChildren(hostContext, element, actualType);
      // what were going to do is merge them - the type is not allowed to constrain things that the child definitions already do (well, if it does, it'll be ignored)
      mergeChildLists(childDefinitions, typeChildDefinitions, definition.getPath(), actualType);
    }

    List<ElementInfo> children = listChildren(element, stack);
    List<String> problematicPaths = assignChildren(hostContext, errors, profile, resource, stack, childDefinitions, children);

    checkCardinalities(errors, profile, element, stack, childDefinitions, children, problematicPaths);
    // 4. check order if any slices are ordered. (todo)

    // 5. inspect each child for validity
    for (ElementInfo ei : children) {
      checkChild(hostContext, errors, profile, definition, resource, element, actualType, stack, inCodeableConcept, checkDisplayInContext, ei, extensionUrl);
    }
  }

  private void mergeChildLists(List<ElementDefinition> master, List<ElementDefinition> additional, String masterPath, String typePath) {
    for (ElementDefinition ed : additional) {
      boolean inMaster = false;
      for (ElementDefinition t : master) {
        String tp = masterPath + ed.getPath().substring(typePath.length());
        if (t.getPath().equals(tp)) {
          inMaster = true;
        }
      }
      if (!inMaster) {
        master.add(ed);
      }
    }


  }

  // todo: the element definition in context might assign a constrained profile for the type?
  public List<ElementDefinition> getActualTypeChildren(ValidatorHostContext hostContext, Element element, String actualType) {
    List<ElementDefinition> childDefinitions;
    StructureDefinition dt = null;
    if (isAbsolute(actualType))
      dt = this.context.fetchResource(StructureDefinition.class, actualType);
    else
      dt = this.context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + actualType);
    if (dt == null)
      throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_ACTUAL_TYPE_, actualType));
    trackUsage(dt, hostContext, element);

    childDefinitions = profileUtilities.getChildMap(dt, dt.getSnapshot().getElement().get(0));
    return childDefinitions;
  }

  public void checkChild(ValidatorHostContext hostContext, List<ValidationMessage> errors, StructureDefinition profile, ElementDefinition definition,
    Element resource, Element element, String actualType, NodeStack stack, boolean inCodeableConcept, boolean checkDisplayInContext, ElementInfo ei, String extensionUrl)
    throws FHIRException, DefinitionException {

    List<String> profiles = new ArrayList<String>();
    if (ei.definition != null) {
      String type = null;
      ElementDefinition typeDefn = null;
      checkMustSupport(profile, ei);

      if (ei.definition.getType().size() == 1 && !"*".equals(ei.definition.getType().get(0).getWorkingCode()) && !"Element".equals(ei.definition.getType().get(0).getWorkingCode())
        && !"BackboneElement".equals(ei.definition.getType().get(0).getWorkingCode())) {
        type = ei.definition.getType().get(0).getWorkingCode();
        // Excluding reference is a kludge to get around versioning issues
        if (ei.definition.getType().get(0).hasProfile()) {
          for (CanonicalType p : ei.definition.getType().get(0).getProfile()) {
            profiles.add(p.getValue());
          }
        }
      } else if (ei.definition.getType().size() == 1 && "*".equals(ei.definition.getType().get(0).getWorkingCode())) {
        String prefix = tail(ei.definition.getPath());
        assert prefix.endsWith("[x]");
        type = ei.getName().substring(prefix.length() - 3);
        if (isPrimitiveType(type))
          type = Utilities.uncapitalize(type);
        if (ei.definition.getType().get(0).hasProfile()) {
          for (CanonicalType p : ei.definition.getType().get(0).getProfile()) {
            profiles.add(p.getValue());
          }
        }
      } else if (ei.definition.getType().size() > 1) {

        String prefix = tail(ei.definition.getPath());
        assert typesAreAllReference(ei.definition.getType()) || ei.definition.hasRepresentation(PropertyRepresentation.TYPEATTR) || prefix.endsWith("[x]") : prefix;

        if (ei.definition.hasRepresentation(PropertyRepresentation.TYPEATTR))
          type = ei.getElement().getType();
        else {
          prefix = prefix.substring(0, prefix.length() - 3);
          for (TypeRefComponent t : ei.definition.getType())
            if ((prefix + Utilities.capitalize(t.getWorkingCode())).equals(ei.getName())) {
              type = t.getWorkingCode();
              // Excluding reference is a kludge to get around versioning issues
              if (t.hasProfile() && !type.equals("Reference"))
                profiles.add(t.getProfile().get(0).getValue());
            }
        }
        if (type == null) {
          TypeRefComponent trc = ei.definition.getType().get(0);
          if (trc.getWorkingCode().equals("Reference"))
            type = "Reference";
          else
            rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), stack.getLiteralPath(), false, I18nConstants.VALIDATION_VAL_PROFILE_NOTYPE, ei.getName(), describeTypes(ei.definition.getType()));
        }
      } else if (ei.definition.getContentReference() != null) {
        typeDefn = resolveNameReference(profile.getSnapshot(), ei.definition.getContentReference());
      } else if (ei.definition.getType().size() == 1 && ("Element".equals(ei.definition.getType().get(0).getWorkingCode()) || "BackboneElement".equals(ei.definition.getType().get(0).getWorkingCode()))) {
        if (ei.definition.getType().get(0).hasProfile()) {
          CanonicalType pu = ei.definition.getType().get(0).getProfile().get(0);
          if (pu.hasExtension(ToolingExtensions.EXT_PROFILE_ELEMENT))
            profiles.add(pu.getValue() + "#" + pu.getExtensionString(ToolingExtensions.EXT_PROFILE_ELEMENT));
          else
            profiles.add(pu.getValue());
        }
      }

      if (type != null) {
        if (type.startsWith("@")) {
          ei.definition = findElement(profile, type.substring(1));
          type = null;
        }
      }
      NodeStack localStack = stack.push(ei.getElement(), ei.count, ei.definition, type == null ? typeDefn : resolveType(type, ei.definition.getType()));
      if (debug) {
        System.out.println("  check " + localStack.getLiteralPath()+" against "+ei.getDefinition().getId()+" in profile "+profile.getUrl());
      }
      String localStackLiterapPath = localStack.getLiteralPath();
      String eiPath = ei.getPath();
      assert (eiPath.equals(localStackLiterapPath)) : "ei.path: " + ei.getPath() + "  -  localStack.getLiteralPath: " + localStackLiterapPath;
      boolean thisIsCodeableConcept = false;
      String thisExtension = null;
      boolean checkDisplay = true;

      checkInvariants(hostContext, errors, profile, ei.definition, resource, ei.getElement(), localStack, true);

      ei.getElement().markValidation(profile, ei.definition);
      boolean elementValidated = false;
      if (type != null) {
        if (isPrimitiveType(type)) {
          checkPrimitive(hostContext, errors, ei.getPath(), type, ei.definition, ei.getElement(), profile, stack);
        } else {
          if (ei.definition.hasFixed()) {
            checkFixedValue(errors, ei.getPath(), ei.getElement(), ei.definition.getFixed(), profile.getUrl(), ei.definition.getSliceName(), null);
          }
          if (ei.definition.hasPattern()) {
            checkFixedValue(errors, ei.getPath(), ei.getElement(), ei.definition.getPattern(), profile.getUrl(), ei.definition.getSliceName(), null, true);
          }
        }
        if (type.equals("Identifier")) {
          checkIdentifier(errors, ei.getPath(), ei.getElement(), ei.definition);
        } else if (type.equals("Coding")) {
          checkCoding(errors, ei.getPath(), ei.getElement(), profile, ei.definition, inCodeableConcept, checkDisplayInContext, stack);
        } else if (type.equals("CodeableConcept")) {
          checkDisplay = checkCodeableConcept(errors, ei.getPath(), ei.getElement(), profile, ei.definition, stack);
          thisIsCodeableConcept = true;
        } else if (type.equals("Reference")) {
          checkReference(hostContext, errors, ei.getPath(), ei.getElement(), profile, ei.definition, actualType, localStack);
          // We only check extensions if we're not in a complex extension or if the element we're dealing with is not defined as part of that complex extension
        } else if (type.equals("Extension")) {
          Element eurl = ei.getElement().getNamedChild("url");
          if (rule(errors, IssueType.INVALID, ei.getPath(), eurl != null, I18nConstants.EXTENSION_EXT_URL_NOTFOUND)) {
            String url = eurl.primitiveValue();
            thisExtension = url;
            if (rule(errors, IssueType.INVALID, ei.getPath(), !Utilities.noString(url), I18nConstants.EXTENSION_EXT_URL_NOTFOUND)) {
              if (rule(errors, IssueType.INVALID, ei.getPath(), (extensionUrl != null) || Utilities.isAbsoluteUrl(url), I18nConstants.EXTENSION_EXT_URL_ABSOLUTE)) {
                checkExtension(hostContext, errors, ei.getPath(), resource, element, ei.getElement(), ei.definition, profile, localStack, stack, extensionUrl);
              }
            }
          }
        } else if (type.equals("Resource")) {
          validateContains(hostContext, errors, ei.getPath(), ei.definition, definition, resource, ei.getElement(), localStack, idStatusForEntry(element, ei)); // if
          elementValidated = true;
          // (str.matches(".*([.,/])work\\1$"))
        } else if (Utilities.isAbsoluteUrl(type)) {
          StructureDefinition defn = context.fetchTypeDefinition(type);
          if (defn != null && hasMapping("http://hl7.org/fhir/terminology-pattern", defn, defn.getSnapshot().getElementFirstRep())) {
            List<String> txtype = getMapping("http://hl7.org/fhir/terminology-pattern", defn, defn.getSnapshot().getElementFirstRep());
            if (txtype.contains("CodeableConcept")) {
              checkTerminologyCodeableConcept(errors, ei.getPath(), ei.getElement(), profile, ei.definition, stack, defn);
              thisIsCodeableConcept = true;
            } else if (txtype.contains("Coding")) {
              checkTerminologyCoding(errors, ei.getPath(), ei.getElement(), profile, ei.definition, inCodeableConcept, checkDisplayInContext, stack, defn);
            }
          }
        }
      } else {
        if (rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), stack.getLiteralPath(), ei.definition != null, I18nConstants.VALIDATION_VAL_CONTENT_UNKNOWN, ei.getName()))
          validateElement(hostContext, errors, profile, ei.definition, null, null, resource, ei.getElement(), type, localStack, false, true, null);
      }
      StructureDefinition p = null;
      String tail = null;
      if (profiles.isEmpty()) {
        if (type != null) {
          p = getProfileForType(type, ei.definition.getType());

          // If dealing with a primitive type, then we need to check the current child against
          // the invariants (constraints) on the current element, because otherwise it only gets
          // checked against the primary type's invariants: LLoyd
          //if (p.getKind() == StructureDefinitionKind.PRIMITIVETYPE) {
          //  checkInvariants(hostContext, errors, ei.path, profile, ei.definition, null, null, resource, ei.element);
          //}

          rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), p != null, I18nConstants.VALIDATION_VAL_NOTYPE, type);
        }
      } else if (profiles.size() == 1) {
        String url = profiles.get(0);
        if (url.contains("#")) {
          tail = url.substring(url.indexOf("#") + 1);
          url = url.substring(0, url.indexOf("#"));
        }
        p = this.context.fetchResource(StructureDefinition.class, url);
        rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), p != null, I18nConstants.VALIDATION_VAL_UNKNOWN_PROFILE, profiles.get(0));
      } else {
        elementValidated = true;
        HashMap<String, List<ValidationMessage>> goodProfiles = new HashMap<String, List<ValidationMessage>>();
        HashMap<String, List<ValidationMessage>> badProfiles = new HashMap<String, List<ValidationMessage>>();
        for (String typeProfile : profiles) {
          String url = typeProfile;
          tail = null;
          if (url.contains("#")) {
            tail = url.substring(url.indexOf("#") + 1);
            url = url.substring(0, url.indexOf("#"));
          }
          p = this.context.fetchResource(StructureDefinition.class, typeProfile);
          if (rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), p != null, I18nConstants.VALIDATION_VAL_UNKNOWN_PROFILE, typeProfile)) {
            List<ValidationMessage> profileErrors = new ArrayList<ValidationMessage>();
            validateElement(hostContext, profileErrors, p, getElementByTail(p, tail), profile, ei.definition, resource, ei.getElement(), type, localStack, thisIsCodeableConcept, checkDisplay, thisExtension);
            if (hasErrors(profileErrors))
              badProfiles.put(typeProfile, profileErrors);
            else
              goodProfiles.put(typeProfile, profileErrors);
          }
        }
        if (goodProfiles.size() == 1) {
          errors.addAll(goodProfiles.values().iterator().next());
        } else if (goodProfiles.size() == 0) {
          rule(errors, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), false, I18nConstants.VALIDATION_VAL_PROFILE_NOMATCH, StringUtils.join("; ", profiles));
          for (String m : badProfiles.keySet()) {
            p = this.context.fetchResource(StructureDefinition.class, m);
            for (ValidationMessage message : badProfiles.get(m)) {
              message.setMessage(message.getMessage() + " (validating against " + p.getUrl() + (p.hasVersion() ? "|" + p.getVersion() : "") + " [" + p.getName() + "])");
              errors.add(message);
            }
          }
        } else {
          warning(errors, IssueType.STRUCTURE, ei.line(), ei.col(), ei.getPath(), false, I18nConstants.VALIDATION_VAL_PROFILE_MULTIPLEMATCHES, StringUtils.join("; ", goodProfiles.keySet()));
          for (String m : goodProfiles.keySet()) {
            p = this.context.fetchResource(StructureDefinition.class, m);
            for (ValidationMessage message : goodProfiles.get(m)) {
              message.setMessage(message.getMessage() + " (validating against " + p.getUrl() + (p.hasVersion() ? "|" + p.getVersion() : "") + " [" + p.getName() + "])");
              errors.add(message);
            }
          }
        }
      }
      if (p != null) {
        trackUsage(p, hostContext, element);

        if (!elementValidated) {
          if (ei.getElement().getSpecial() == SpecialElement.BUNDLE_ENTRY || ei.getElement().getSpecial() == SpecialElement.BUNDLE_OUTCOME || ei.getElement().getSpecial() == SpecialElement.PARAMETER)
            validateElement(hostContext, errors, p, getElementByTail(p, tail), profile, ei.definition, ei.getElement(), ei.getElement(), type, localStack, thisIsCodeableConcept, checkDisplay, thisExtension);
          else
            validateElement(hostContext, errors, p, getElementByTail(p, tail), profile, ei.definition, resource, ei.getElement(), type, localStack, thisIsCodeableConcept, checkDisplay, thisExtension);
        }
        int index = profile.getSnapshot().getElement().indexOf(ei.definition);
        if (index < profile.getSnapshot().getElement().size() - 1) {
          String nextPath = profile.getSnapshot().getElement().get(index + 1).getPath();
          if (!nextPath.equals(ei.definition.getPath()) && nextPath.startsWith(ei.definition.getPath()))
            validateElement(hostContext, errors, profile, ei.definition, null, null, resource, ei.getElement(), type, localStack, thisIsCodeableConcept, checkDisplay, thisExtension);
        }
      }
    }
  }

  private void trackUsage(StructureDefinition profile, ValidatorHostContext hostContext, Element element) {
    if (tracker != null) {
      tracker.recordProfileUsage(profile, hostContext.getAppContext(), element);
    }
  }

  private boolean hasMapping(String url, StructureDefinition defn, ElementDefinition elem) {
    String id = null;
    for (StructureDefinitionMappingComponent m : defn.getMapping()) {
      if (url.equals(m.getUri())) {
        id = m.getIdentity();
        break;
      }
    }
    if (id != null) {
      for (ElementDefinitionMappingComponent m : elem.getMapping()) {
        if (id.equals(m.getIdentity())) {
          return true;
        }
      }

    }
    return false;
  }

  private List<String> getMapping(String url, StructureDefinition defn, ElementDefinition elem) {
    List<String> res = new ArrayList<>();
    String id = null;
    for (StructureDefinitionMappingComponent m : defn.getMapping()) {
      if (url.equals(m.getUri())) {
        id = m.getIdentity();
        break;
      }
    }
    if (id != null) {
      for (ElementDefinitionMappingComponent m : elem.getMapping()) {
        if (id.equals(m.getIdentity())) {
          res.add(m.getMap());
        }
      }
    }
    return res;
  }

  public void checkMustSupport(StructureDefinition profile, ElementInfo ei) {
    String usesMustSupport = profile.getUserString("usesMustSupport");
    if (usesMustSupport == null) {
      usesMustSupport = "N";
      for (ElementDefinition pe : profile.getSnapshot().getElement()) {
        if (pe.getMustSupport()) {
          usesMustSupport = "Y";
          break;
        }
      }
      profile.setUserData("usesMustSupport", usesMustSupport);
    }
    if (usesMustSupport.equals("Y")) {
      String elementSupported = ei.getElement().getUserString("elementSupported");
      if (elementSupported == null || ei.definition.getMustSupport())
        if (ei.definition.getMustSupport()) {
          ei.getElement().setUserData("elementSupported", "Y");
        }
    }
  }

  public void checkCardinalities(List<ValidationMessage> errors, StructureDefinition profile, Element element, NodeStack stack,
    List<ElementDefinition> childDefinitions, List<ElementInfo> children, List<String> problematicPaths) throws DefinitionException {
    // 3. report any definitions that have a cardinality problem
    for (ElementDefinition ed : childDefinitions) {
      if (ed.getRepresentation().isEmpty()) { // ignore xml attributes
        int count = 0;
        List<ElementDefinition> slices = null;
        if (ed.hasSlicing())
          slices = profileUtilities.getSliceList(profile, ed);
        for (ElementInfo ei : children)
          if (ei.definition == ed)
            count++;
          else if (slices != null) {
            for (ElementDefinition sed : slices) {
              if (ei.definition == sed) {
                count++;
                break;
              }
            }
          }
        String location = "Profile " + profile.getUrl() + ", Element '" + stack.getLiteralPath() + "." + tail(ed.getPath()) + (ed.hasSliceName() ? "[" + ed.getSliceName() + (ed.hasLabel() ? " (" + ed.getLabel() + ")" : "") + "]" : "") + "'";
        if (ed.getMin() > 0) {
          if (problematicPaths.contains(ed.getPath()))
            hint(errors, IssueType.NOTSUPPORTED, element.line(), element.col(), stack.getLiteralPath(), count >= ed.getMin(), I18nConstants.VALIDATION_VAL_PROFILE_NOCHECKMIN, location, Integer.toString(ed.getMin()));
          else
            rule(errors, IssueType.STRUCTURE, element.line(), element.col(), stack.getLiteralPath(), count >= ed.getMin(), I18nConstants.VALIDATION_VAL_PROFILE_MINIMUM, location, Integer.toString(ed.getMin()), Integer.toString(count));
        }
        if (ed.hasMax() && !ed.getMax().equals("*")) {
          if (problematicPaths.contains(ed.getPath()))
            hint(errors, IssueType.NOTSUPPORTED, element.line(), element.col(), stack.getLiteralPath(), count <= Integer.parseInt(ed.getMax()), I18nConstants.VALIDATION_VAL_PROFILE_NOCHECKMAX, location, ed.getMax());
          else
            rule(errors, IssueType.STRUCTURE, element.line(), element.col(), stack.getLiteralPath(), count <= Integer.parseInt(ed.getMax()), I18nConstants.VALIDATION_VAL_PROFILE_MAXIMUM, location, ed.getMax(), Integer.toString(count));
        }
      }
    }
  }

  public List<String> assignChildren(ValidatorHostContext hostContext, List<ValidationMessage> errors, StructureDefinition profile, Element resource,
    NodeStack stack, List<ElementDefinition> childDefinitions, List<ElementInfo> children) throws DefinitionException {
    // 2. assign children to a definition
    // for each definition, for each child, check whether it belongs in the slice
    ElementDefinition slicer = null;
    boolean unsupportedSlicing = false;
    List<String> problematicPaths = new ArrayList<String>();
    String slicingPath = null;
    int sliceOffset = 0;
    for (int i = 0; i < childDefinitions.size(); i++) {
      ElementDefinition ed = childDefinitions.get(i);
      boolean childUnsupportedSlicing = false;
      boolean process = true;
      if (ed.hasSlicing() && !ed.getSlicing().getOrdered())
        slicingPath = ed.getPath();
      else if (slicingPath != null && ed.getPath().equals(slicingPath))
        ; // nothing
      else if (slicingPath != null && !ed.getPath().startsWith(slicingPath))
        slicingPath = null;
      // where are we with slicing
      if (ed.hasSlicing()) {
        if (slicer != null && slicer.getPath().equals(ed.getPath())) {
          String errorContext = "profile " + profile.getUrl();
          if (!resource.getChildValue("id").isEmpty())
            errorContext += "; instance " + resource.getChildValue("id");
          throw new DefinitionException(context.formatMessage(I18nConstants.SLICE_ENCOUNTERED_MIDWAY_THROUGH_SET_PATH___ID___, slicer.getPath(), slicer.getId(), errorContext));
        }
        slicer = ed;
        process = false;
        sliceOffset = i;
      } else if (slicer != null && !slicer.getPath().equals(ed.getPath()))
        slicer = null;

      for (ElementInfo ei : children) {
        if (ei.sliceInfo == null) {
          ei.sliceInfo = new ArrayList<>();
        }
        unsupportedSlicing = matchSlice(hostContext, errors, ei.sliceInfo, profile, stack, slicer, unsupportedSlicing, problematicPaths, sliceOffset, i, ed, childUnsupportedSlicing, ei);
      }
    }
    int last = -1;
    int lastSlice = -1;
    for (ElementInfo ei : children) {
      String sliceInfo = "";
      if (slicer != null)
        sliceInfo = " (slice: " + slicer.getPath() + ")";
      if (!unsupportedSlicing)
        if (ei.additionalSlice && ei.definition != null) {
          if (ei.definition.getSlicing().getRules().equals(ElementDefinition.SlicingRules.OPEN) ||
            ei.definition.getSlicing().getRules().equals(ElementDefinition.SlicingRules.OPENATEND) && true /* TODO: replace "true" with condition to check that this element is at "end" */) {
            slicingHint(errors, IssueType.INFORMATIONAL, ei.line(), ei.col(), ei.getPath(), false,
              context.formatMessage(I18nConstants.THIS_ELEMENT_DOES_NOT_MATCH_ANY_KNOWN_SLICE_,
                profile == null ? "" : " defined in the profile " + profile.getUrl()),
              context.formatMessage(I18nConstants.THIS_ELEMENT_DOES_NOT_MATCH_ANY_KNOWN_SLICE_, profile == null ? "" : I18nConstants.DEFINED_IN_THE_PROFILE + profile.getUrl()) + errorSummaryForSlicingAsHtml(ei.sliceInfo));
          } else if (ei.definition.getSlicing().getRules().equals(ElementDefinition.SlicingRules.CLOSED)) {
            rule(errors, IssueType.INVALID, ei.line(), ei.col(), ei.getPath(), false, I18nConstants.VALIDATION_VAL_PROFILE_NOTSLICE, (profile == null ? "" : " defined in the profile " + profile.getUrl()), errorSummaryForSlicing(ei.sliceInfo));
          }
        } else {
          // Don't raise this if we're in an abstract profile, like Resource
          if (!profile.getAbstract())
            rule(errors, IssueType.NOTSUPPORTED, ei.line(), ei.col(), ei.getPath(), (ei.definition != null), I18nConstants.VALIDATION_VAL_PROFILE_NOTALLOWED, profile.getUrl());
        }
      // TODO: Should get the order of elements correct when parsing elements that are XML attributes vs. elements
      boolean isXmlAttr = false;
      if (ei.definition != null) {
        for (Enumeration<PropertyRepresentation> r : ei.definition.getRepresentation()) {
          if (r.getValue() == PropertyRepresentation.XMLATTR) {
            isXmlAttr = true;
            break;
          }
        }
      }

      if (!ToolingExtensions.readBoolExtension(profile, "http://hl7.org/fhir/StructureDefinition/structuredefinition-xml-no-order")) {
        boolean ok = (ei.definition == null) || (ei.index >= last) || isXmlAttr;
        rule(errors, IssueType.INVALID, ei.line(), ei.col(), ei.getPath(), ok, I18nConstants.VALIDATION_VAL_PROFILE_OUTOFORDER, profile.getUrl(), ei.getName());
      }
      if (ei.slice != null && ei.index == last && ei.slice.getSlicing().getOrdered())
        rule(errors, IssueType.INVALID, ei.line(), ei.col(), ei.getPath(), (ei.definition == null) || (ei.sliceindex >= lastSlice) || isXmlAttr, I18nConstants.VALIDATION_VAL_PROFILE_SLICEORDER, profile.getUrl(), ei.getName());
      if (ei.definition == null || !isXmlAttr)
        last = ei.index;
      if (ei.slice != null)
        lastSlice = ei.sliceindex;
      else
        lastSlice = -1;
    }
    return problematicPaths;
  }

  public List<ElementInfo> listChildren(Element element, NodeStack stack) {
    // 1. List the children, and remember their exact path (convenience)
    List<ElementInfo> children = new ArrayList<ElementInfo>();
    ChildIterator iter = new ChildIterator(this, stack.getLiteralPath(), element);
    while (iter.next())
      children.add(new ElementInfo(iter.name(), iter.element(), iter.path(), iter.count()));
    return children;
  }

  public void checkInvariants(ValidatorHostContext hostContext, List<ValidationMessage> errors, StructureDefinition profile, ElementDefinition definition, Element resource, Element element, NodeStack stack, boolean onlyNonInherited) throws FHIRException {
    checkInvariants(hostContext, errors, stack.getLiteralPath(), profile, definition, null, null, resource, element, onlyNonInherited);
  }

  public boolean matchSlice(ValidatorHostContext hostContext, List<ValidationMessage> errors, List<ValidationMessage> sliceInfo, StructureDefinition profile, NodeStack stack,
    ElementDefinition slicer, boolean unsupportedSlicing, List<String> problematicPaths, int sliceOffset, int i, ElementDefinition ed,
    boolean childUnsupportedSlicing, ElementInfo ei) {
    boolean match = false;
    if (slicer == null || slicer == ed) {
      match = nameMatches(ei.getName(), tail(ed.getPath()));
    } else {
      if (nameMatches(ei.getName(), tail(ed.getPath())))
        try {
          match = sliceMatches(hostContext, ei.getElement(), ei.getPath(), slicer, ed, profile, errors, sliceInfo, stack);
          if (match) {
            ei.slice = slicer;

            // Since a defined slice was found, this is not an additional (undefined) slice.
            ei.additionalSlice = false;
          } else if (ei.slice == null) {
            // if the specified slice is undefined, keep track of the fact this is an additional (undefined) slice, but only if a slice wasn't found previously
            ei.additionalSlice = true;
          }
        } catch (FHIRException e) {
          rule(errors, IssueType.PROCESSING, ei.line(), ei.col(), ei.getPath(), false, e.getMessage());
          unsupportedSlicing = true;
          childUnsupportedSlicing = true;
        }
    }
    if (match) {
      boolean isOk = ei.definition == null || ei.definition == slicer || (ei.definition.getPath().endsWith("[x]") && ed.getPath().startsWith(ei.definition.getPath().replace("[x]", "")));
      if (rule(errors, IssueType.INVALID, ei.line(), ei.col(), ei.getPath(), isOk, I18nConstants.VALIDATION_VAL_PROFILE_MATCHMULTIPLE, profile.getUrl(), (ei.definition == null || !ei.definition.hasSliceName() ? "" : ei.definition.getSliceName()), (ed.hasSliceName() ? ed.getSliceName() : ""))) {
        ei.definition = ed;
        if (ei.slice == null) {
          ei.index = i;
        } else {
          ei.index = sliceOffset;
          ei.sliceindex = i - (sliceOffset + 1);
        }
      }
    } else if (childUnsupportedSlicing) {
      problematicPaths.add(ed.getPath());
    }
    return unsupportedSlicing;
  }

  private ElementDefinition getElementByTail(StructureDefinition p, String tail) throws DefinitionException {
    if (tail == null)
      return p.getSnapshot().getElement().get(0);
    for (ElementDefinition t : p.getSnapshot().getElement()) {
      if (tail.equals(t.getId()))
        return t;
    }
    throw new DefinitionException(context.formatMessage(I18nConstants.UNABLE_TO_FIND_ELEMENT_WITH_ID_, tail));
  }

  private IdStatus idStatusForEntry(Element ep, ElementInfo ei) {
    if (isBundleEntry(ei.getPath())) {
      Element req = ep.getNamedChild("request");
      Element resp = ep.getNamedChild("response");
      Element fullUrl = ep.getNamedChild("fullUrl");
      Element method = null;
      Element url = null;
      if (req != null) {
        method = req.getNamedChild("method");
        url = req.getNamedChild("url");
      }
      if (resp != null) {
        return IdStatus.OPTIONAL;
      }
      if (method == null) {
        if (fullUrl == null)
          return IdStatus.REQUIRED;
        else if (fullUrl.primitiveValue().startsWith("urn:uuid:") || fullUrl.primitiveValue().startsWith("urn:oid:"))
          return IdStatus.OPTIONAL;
        else
          return IdStatus.REQUIRED;
      } else {
        String s = method.primitiveValue();
        if (s.equals("PUT")) {
          if (url == null)
            return IdStatus.REQUIRED;
          else
            return IdStatus.OPTIONAL; // or maybe prohibited? not clear
        } else if (s.equals("POST"))
          return IdStatus.OPTIONAL; // this should be prohibited, but see task 9102
        else // actually, we should never get to here; a bundle entry with method get/delete should not have a resource
          return IdStatus.OPTIONAL;
      }
    } else if (isParametersEntry(ei.getPath()) || isBundleOutcome(ei.getPath()))
      return IdStatus.OPTIONAL;
    else
      return IdStatus.REQUIRED;
  }

  private void checkInvariants(ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, StructureDefinition profile, ElementDefinition ed, String typename, String typeProfile, Element resource, Element element, boolean onlyNonInherited) throws FHIRException, FHIRException {
    if (noInvariantChecks)
      return;

    for (ElementDefinitionConstraintComponent inv : ed.getConstraint()) {
      if (inv.hasExpression() && (!onlyNonInherited || !inv.hasSource() || !isInheritedProfile(profile, inv.getSource()))) {
        @SuppressWarnings("unchecked")
        Set<String> invList = executionId.equals(element.getUserString(EXECUTION_ID)) ? (Set<String>) element.getUserData(EXECUTED_CONSTRAINT_LIST) : null;
        if (invList == null) {
          invList = new HashSet<>();
          element.setUserData(EXECUTED_CONSTRAINT_LIST, invList);
          element.setUserData(EXECUTION_ID, executionId);
        }
        if (!invList.contains(inv.getKey())) {
          invList.add(inv.getKey());
          checkInvariant(hostContext, errors, path, profile, resource, element, inv);
        } else {
          //System.out.println("Skip "+inv.getKey()+" on "+path);
        }
      }
    }
  }

  private boolean isInheritedProfile(StructureDefinition profile, String source) {
    if (source.equals(profile.getUrl())) {
      return false;
    }
    while (profile != null) {
      profile = context.fetchResource(StructureDefinition.class, profile.getBaseDefinition());
      if (profile != null) {
        if (source.equals(profile.getUrl())) {
          return true;
        }
      }
    }
    return false;
  }

  public void checkInvariant(ValidatorHostContext hostContext, List<ValidationMessage> errors, String path, StructureDefinition profile, Element resource, Element element, ElementDefinitionConstraintComponent inv) throws FHIRException {
    ExpressionNode n = (ExpressionNode) inv.getUserData("validator.expression.cache");
    if (n == null) {
      long t = System.nanoTime();
      try {
        n = fpe.parse(fixExpr(inv.getExpression()));
      } catch (FHIRLexerException e) {
        throw new FHIRException(context.formatMessage(I18nConstants.PROBLEM_PROCESSING_EXPRESSION__IN_PROFILE__PATH__, inv.getExpression(), profile.getUrl(), path, e.getMessage()));
      }
      fpeTime = fpeTime + (System.nanoTime() - t);
      inv.setUserData("validator.expression.cache", n);
    }

    String msg;
    boolean ok;
    try {
      long t = System.nanoTime();
      ok = fpe.evaluateToBoolean(hostContext, resource, hostContext.getRootResource(), element, n);
      fpeTime = fpeTime + (System.nanoTime() - t);
      msg = fpe.forLog();
    } catch (Exception ex) {
      ok = false;
      msg = ex.getMessage();
    }
    if (!ok) {
      if (!Utilities.noString(msg))
        msg = " (" + msg + ")";
      if (inv.hasExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-bestpractice") &&
        ToolingExtensions.readBooleanExtension(inv, "http://hl7.org/fhir/StructureDefinition/elementdefinition-bestpractice")) {
        if (bpWarnings == BestPracticeWarningLevel.Hint)
          hint(errors, IssueType.INVARIANT, element.line(), element.col(), path, ok, inv.getKey() + ": " + inv.getHuman() + msg + " [" + n.toString() + "]");
        else if (bpWarnings == BestPracticeWarningLevel.Warning)
          warning(errors, IssueType.INVARIANT, element.line(), element.col(), path, ok, inv.getKey() + ": " + inv.getHuman() + msg + " [" + n.toString() + "]");
        else if (bpWarnings == BestPracticeWarningLevel.Error)
          rule(errors, IssueType.INVARIANT, element.line(), element.col(), path, ok, inv.getKey() + ": " + inv.getHuman() + msg + " [" + n.toString() + "]");
      } else if (inv.getSeverity() == ConstraintSeverity.ERROR) {
        rule(errors, IssueType.INVARIANT, element.line(), element.col(), path, ok, inv.getKey() + ": " + inv.getHuman() + msg + " [" + n.toString() + "]");
      } else if (inv.getSeverity() == ConstraintSeverity.WARNING) {
        warning(errors, IssueType.INVARIANT, element.line(), element.line(), path, ok, inv.getKey() + ": " + inv.getHuman() + msg + " [" + n.toString() + "]");
      }
    }
  }

  private void validateMessage(List<ValidationMessage> errors, List<Element> entries, Element messageHeader, NodeStack stack, String fullUrl, String id) {
    // first entry must be a messageheader
    if (rule(errors, IssueType.INVALID, messageHeader.line(), messageHeader.col(), stack.getLiteralPath(), messageHeader.getType().equals("MessageHeader"), I18nConstants.VALIDATION_BUNDLE_MESSAGE)) {
      List<Element> elements = messageHeader.getChildren("focus");
      for (Element elem : elements)
        validateBundleReference(errors, entries, elem, "MessageHeader Data", stack.push(elem, -1, null, null), fullUrl, "MessageHeader", id);
    }
  }

  private void validateObservation(List<ValidationMessage> errors, Element element, NodeStack stack) {
    // all observations should have a subject, a performer, and a time

    bpCheck(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), element.getNamedChild("subject") != null, I18nConstants.ALL_OBSERVATIONS_SHOULD_HAVE_A_SUBJECT);
    List<Element> performers = new ArrayList<>();
    element.getNamedChildren("performer", performers);
    bpCheck(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), performers.size() > 0, I18nConstants.ALL_OBSERVATIONS_SHOULD_HAVE_A_PERFORMER);
    bpCheck(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), element.getNamedChild("effectiveDateTime") != null || element.getNamedChild("effectivePeriod") != null, I18nConstants.ALL_OBSERVATIONS_SHOULD_HAVE_AN_EFFECTIVEDATETIME_OR_AN_EFFECTIVEPERIOD);
  }

  /*
   * The actual base entry point for internal use (re-entrant)
   */
  private void validateResource(ValidatorHostContext hostContext, List<ValidationMessage> errors, Element resource, Element element, StructureDefinition defn, IdStatus idstatus, NodeStack stack) throws FHIRException {
    assert stack != null;
    assert resource != null;
    boolean ok = true;
    String resourceName = element.getType(); // todo: consider namespace...?
    if (defn == null) {
      long t = System.nanoTime();
      defn = element.getProperty().getStructure();
      if (defn == null)
        defn = context.fetchResource(StructureDefinition.class, "http://hl7.org/fhir/StructureDefinition/" + resourceName);
      sdTime = sdTime + (System.nanoTime() - t);
      ok = rule(errors, IssueType.INVALID, element.line(), element.col(), stack.addToLiteralPath(resourceName), defn != null, I18nConstants.VALIDATION_VAL_PROFILE_NODEFINITION, resourceName);
    }

    String type = defn.getKind() == StructureDefinitionKind.LOGICAL ? defn.getId() : defn.getType();
    // special case: we have a bundle, and the profile is not for a bundle. We'll try the first entry instead
    if (!type.equals(resourceName) && resourceName.equals("Bundle")) {
      NodeStack first = getFirstEntry(stack);
      if (first != null && first.getElement().getType().equals(type)) {
        element = first.element;
        stack = first;
        resourceName = element.getType();
        idstatus = IdStatus.OPTIONAL; // why?
      }
      // todo: validate everything in this bundle.
    }
    ok = rule(errors, IssueType.INVALID, -1, -1, stack.getLiteralPath(), type.equals(resourceName), I18nConstants.VALIDATION_VAL_PROFILE_WRONGTYPE, type, resourceName);

    if (ok) {
      if (idstatus == IdStatus.REQUIRED && (element.getNamedChild("id") == null))
        rule(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.RESOURCE_RES_ID_MISSING);
      else if (idstatus == IdStatus.PROHIBITED && (element.getNamedChild("id") != null))
        rule(errors, IssueType.INVALID, element.line(), element.col(), stack.getLiteralPath(), false, I18nConstants.RESOURCE_RES_ID_PROHIBITED);
      start(hostContext, errors, element, element, defn, stack); // root is both definition and type
    }
  }

  private NodeStack getFirstEntry(NodeStack bundle) {
    List<Element> list = new ArrayList<Element>();
    bundle.getElement().getNamedChildren("entry", list);
    if (list.isEmpty())
      return null;
    Element resource = list.get(0).getNamedChild("resource");
    if (resource == null)
      return null;
    else {
      NodeStack entry = bundle.push(list.get(0), 0, list.get(0).getProperty().getDefinition(), list.get(0).getProperty().getDefinition());
      return entry.push(resource, -1, resource.getProperty().getDefinition(), context.fetchTypeDefinition(resource.fhirType()).getSnapshot().getElementFirstRep());
    }
  }

  private void validateSections(List<ValidationMessage> errors, List<Element> entries, Element focus, NodeStack stack, String fullUrl, String id) {
    List<Element> sections = new ArrayList<Element>();
    focus.getNamedChildren("section", sections);
    int i = 1;
    for (Element section : sections) {
      NodeStack localStack = stack.push(section, i, null, null);

      // technically R4+, but there won't be matches from before that
      validateDocumentReference(errors, entries, section, stack, fullUrl, id, false, "author", "Section");
      validateDocumentReference(errors, entries, section, stack, fullUrl, id, false, "focus", "Section");

      List<Element> sectionEntries = new ArrayList<Element>();
      section.getNamedChildren("entry", sectionEntries);
      int j = 1;
      for (Element sectionEntry : sectionEntries) {
        NodeStack localStack2 = localStack.push(sectionEntry, j, null, null);
        validateBundleReference(errors, entries, sectionEntry, "Section Entry", localStack2, fullUrl, "Composition", id);
        j++;
      }
      validateSections(errors, entries, section, localStack, fullUrl, id);
      i++;
    }
  }

  private boolean valueMatchesCriteria(Element value, ElementDefinition criteria, StructureDefinition profile) throws FHIRException {
    if (criteria.hasFixed()) {
      List<ValidationMessage> msgs = new ArrayList<ValidationMessage>();
      checkFixedValue(msgs, "{virtual}", value, criteria.getFixed(), profile.getUrl(), "value", null);
      return msgs.size() == 0;
    } else if (criteria.hasBinding() && criteria.getBinding().getStrength() == BindingStrength.REQUIRED && criteria.getBinding().hasValueSet()) {
      throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SLICE_MATCHING__SLICE_MATCHING_BY_VALUE_SET_NOT_DONE));
    } else {
      throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SLICE_MATCHING__NO_FIXED_VALUE_OR_REQUIRED_VALUE_SET));
    }
  }

  private boolean yearIsValid(String v) {
    if (v == null) {
      return false;
    }
    try {
      int i = Integer.parseInt(v.substring(0, Math.min(4, v.length())));
      return i >= 1800 && i <= thisYear() + 80;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private int thisYear() {
    return Calendar.getInstance().get(Calendar.YEAR);
  }

  public class NodeStack {
    private ElementDefinition definition;
    private Element element;
    private ElementDefinition extension;
    private String literalPath; // xpath format
    private List<String> logicalPaths; // dotted format, various entry points
    private NodeStack parent;
    private ElementDefinition type;
    private String workingLang;

    public NodeStack() {
    }

    public NodeStack(Element element) {
      this.element = element;
      literalPath = element.getName();
      workingLang = validationLanguage;
      if (!element.getName().equals(element.fhirType())) {
        logicalPaths = new ArrayList<>();
        logicalPaths.add(element.fhirType());
      }
    }

    public NodeStack(Element element, String refPath) {
      this.element = element;
      literalPath = refPath + "->" + element.getName();
      workingLang = validationLanguage;
    }

    public String addToLiteralPath(String... path) {
      StringBuilder b = new StringBuilder();
      b.append(getLiteralPath());
      for (String p : path) {
        if (p.startsWith(":")) {
          b.append("[");
          b.append(p.substring(1));
          b.append("]");
        } else {
          b.append(".");
          b.append(p);
        }
      }
      return b.toString();
    }

    private ElementDefinition getDefinition() {
      return definition;
    }

    private Element getElement() {
      return element;
    }

    protected String getLiteralPath() {
      return literalPath == null ? "" : literalPath;
    }

    private List<String> getLogicalPaths() {
      return logicalPaths == null ? new ArrayList<String>() : logicalPaths;
    }

    private ElementDefinition getType() {
      return type;
    }

    private NodeStack pushTarget(Element element, int count, ElementDefinition definition, ElementDefinition type) {
      return pushInternal(element, count, definition, type, "->");
    }

    private NodeStack push(Element element, int count, ElementDefinition definition, ElementDefinition type) {
      return pushInternal(element, count, definition, type, ".");
    }

    private NodeStack pushInternal(Element element, int count, ElementDefinition definition, ElementDefinition type, String sep) {
      NodeStack res = new NodeStack();
      res.parent = this;
      res.workingLang = this.workingLang;
      res.element = element;
      res.definition = definition;
      res.literalPath = getLiteralPath() + sep + element.getName();
      if (count > -1)
        res.literalPath = res.literalPath + "[" + Integer.toString(count) + "]";
      else if (element.getSpecial() == null && element.getProperty().isList())
        res.literalPath = res.literalPath + "[0]";
      else if (element.getProperty().isChoice()) {
        String n = res.literalPath.substring(res.literalPath.lastIndexOf(".") + 1);
        String en = element.getProperty().getName();
        en = en.substring(0, en.length() - 3);
        String t = n.substring(en.length());
        if (isPrimitiveType(Utilities.uncapitalize(t)))
          t = Utilities.uncapitalize(t);
        res.literalPath = res.literalPath.substring(0, res.literalPath.lastIndexOf(".")) + "." + en + ".ofType(" + t + ")";
      }
      res.logicalPaths = new ArrayList<String>();
      if (type != null) {
        // type will be bull if we on a stitching point of a contained resource, or if....
        res.type = type;
        String tn = res.type.getPath();
        String t = tail(definition.getPath());
        if ("Resource".equals(tn)) {
          tn = element.fhirType();
        }
        for (String lp : getLogicalPaths()) {
          res.logicalPaths.add(lp + "." + t);
          if (t.endsWith("[x]"))
            res.logicalPaths.add(lp + "." + t.substring(0, t.length() - 3) + type.getPath());
        }
        res.logicalPaths.add(tn);
      } else if (definition != null) {
        for (String lp : getLogicalPaths()) {
          res.logicalPaths.add(lp + "." + element.getName());
        }
        res.logicalPaths.add(definition.typeSummary());
      } else
        res.logicalPaths.addAll(getLogicalPaths());
      return res;
    }

    private void setType(ElementDefinition type) {
      this.type = type;
    }
  }

  public String reportTimes() {
    String s = String.format("Times (ms): overall = %d, tx = %d, sd = %d, load = %d, fpe = %d", overall / 1000000, txTime / 1000000, sdTime / 1000000, loadTime / 1000000, fpeTime / 1000000);
    overall = 0;
    txTime = 0;
    sdTime = 0;
    loadTime = 0;
    fpeTime = 0;
    return s;
  }

  public boolean isNoBindingMsgSuppressed() {
    return noBindingMsgSuppressed;
  }

  public IResourceValidator setNoBindingMsgSuppressed(boolean noBindingMsgSuppressed) {
    this.noBindingMsgSuppressed = noBindingMsgSuppressed;
    return this;
  }


  public boolean isNoTerminologyChecks() {
    return noTerminologyChecks;
  }

  public IResourceValidator setNoTerminologyChecks(boolean noTerminologyChecks) {
    this.noTerminologyChecks = noTerminologyChecks;
    return this;
  }

  public void checkAllInvariants() {
    for (StructureDefinition sd : context.allStructures()) {
      if (sd.getDerivation() == TypeDerivationRule.SPECIALIZATION) {
        for (ElementDefinition ed : sd.getSnapshot().getElement()) {
          for (ElementDefinitionConstraintComponent inv : ed.getConstraint()) {
            if (inv.hasExpression()) {
              try {
                ExpressionNode n = (ExpressionNode) inv.getUserData("validator.expression.cache");
                if (n == null) {
                  n = fpe.parse(fixExpr(inv.getExpression()));
                  inv.setUserData("validator.expression.cache", n);
                }
                fpe.check(null, sd.getKind() == StructureDefinitionKind.RESOURCE ? sd.getType() : "DomainResource", ed.getPath(), n);
              } catch (Exception e) {
                System.out.println("Error processing structure [" + sd.getId() + "] path " + ed.getPath() + ":" + inv.getKey() + " ('" + inv.getExpression() + "'): " + e.getMessage());
              }
            }
          }
        }
      }
    }
  }

  private String fixExpr(String expr) {
    // this is a hack work around for past publication of wrong FHIRPath expressions
    // R4
    // waiting for 4.0.2
    if ("probability is decimal implies (probability as decimal) <= 100".equals(expr)) {
      return "probablility.empty() or ((probability is decimal) implies ((probability as decimal) <= 100))";
    }

    // handled in 4.0.1
    if ("(component.empty() and hasMember.empty()) implies (dataAbsentReason or value)".equals(expr))
      return "(component.empty() and hasMember.empty()) implies (dataAbsentReason.exists() or value.exists())";
    if ("isModifier implies isModifierReason.exists()".equals(expr))
      return "(isModifier.exists() and isModifier) implies isModifierReason.exists()";
    if ("(%resource.kind = 'logical' or element.first().path.startsWith(%resource.type)) and (element.tail().not() or  element.tail().all(path.startsWith(%resource.differential.element.first().path.replaceMatches('\\\\..*','')&'.')))".equals(expr))
      return "(%resource.kind = 'logical' or element.first().path.startsWith(%resource.type)) and (element.tail().empty() or  element.tail().all(path.startsWith(%resource.differential.element.first().path.replaceMatches('\\\\..*','')&'.')))";
    if ("differential.element.all(id) and differential.element.id.trace('ids').isDistinct()".equals(expr))
      return "differential.element.all(id.exists()) and differential.element.id.trace('ids').isDistinct()";
    if ("snapshot.element.all(id) and snapshot.element.id.trace('ids').isDistinct()".equals(expr))
      return "snapshot.element.all(id.exists()) and snapshot.element.id.trace('ids').isDistinct()";

    // R3
    if ("(code or value.empty()) and (system.empty() or system = 'urn:iso:std:iso:4217')".equals(expr))
      return "(code.exists() or value.empty()) and (system.empty() or system = 'urn:iso:std:iso:4217')";
    if ("value.empty() or code!=component.code".equals(expr))
      return "value.empty() or (code in component.code).not()";
    if ("(code or value.empty()) and (system.empty() or system = %ucum) and (value.empty() or value > 0)".equals(expr))
      return "(code.exists() or value.empty()) and (system.empty() or system = %ucum) and (value.empty() or value > 0)";
    if ("element.all(definition and min and max)".equals(expr))
      return "element.all(definition.exists() and min.exists() and max.exists())";
    if ("telecom or endpoint".equals(expr))
      return "telecom.exists() or endpoint.exists()";
    if ("(code or value.empty()) and (system.empty() or system = %ucum) and (value.empty() or value > 0)".equals(expr))
      return "(code.exists() or value.empty()) and (system.empty() or system = %ucum) and (value.empty() or value > 0)";
    if ("searchType implies type = 'string'".equals(expr))
      return "searchType.exists() implies type = 'string'";
    if ("abatement.empty() or (abatement as boolean).not()  or clinicalStatus='resolved' or clinicalStatus='remission' or clinicalStatus='inactive'".equals(expr))
      return "abatement.empty() or (abatement is boolean).not() or (abatement as boolean).not() or (clinicalStatus = 'resolved') or (clinicalStatus = 'remission') or (clinicalStatus = 'inactive')";
    if ("(component.empty() and related.empty()) implies (dataAbsentReason or value)".equals(expr))
      return "(component.empty() and related.empty()) implies (dataAbsentReason.exists() or value.exists())";

    if ("".equals(expr))
      return "";
    return expr;
  }

  public IEvaluationContext getExternalHostServices() {
    return externalHostServices;
  }

  public String getValidationLanguage() {
    return validationLanguage;
  }

  public void setValidationLanguage(String validationLanguage) {
    this.validationLanguage = validationLanguage;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
}
