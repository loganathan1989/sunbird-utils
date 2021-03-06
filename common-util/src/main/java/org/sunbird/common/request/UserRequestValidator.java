package org.sunbird.common.request;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.ProjectUtil.AddressType;
import org.sunbird.common.models.util.StringFormatter;
import org.sunbird.common.responsecode.ResponseCode;

/** @author Amit Kumar */
public class UserRequestValidator {

  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();

  private UserRequestValidator() {}

  /**
   * This method will validate create user data.
   *
   * @param userRequest Request
   */
  public static void validateCreateUser(Request userRequest) {
    externalIdsValidation(userRequest, JsonKey.CREATE);
    fieldsNotAllowed(
        Arrays.asList(
            JsonKey.REGISTERED_ORG_ID,
            JsonKey.ROOT_ORG_ID,
            JsonKey.PROVIDER,
            JsonKey.EXTERNAL_ID,
            JsonKey.EXTERNAL_ID_PROVIDER,
            JsonKey.EXTERNAL_ID_TYPE,
            JsonKey.ID_TYPE),
        userRequest);
    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.USERNAME))) {
      throw new ProjectCommonException(
          ResponseCode.userNameRequired.getErrorCode(),
          ResponseCode.userNameRequired.getErrorMessage(),
          ERROR_CODE);
    }
    createUserBasicValidation(userRequest);
    phoneValidation(userRequest);
    addressValidation(userRequest);
    educationValidation(userRequest);
    jobProfileValidation(userRequest);
    validateWebPages(userRequest);
  }

  public static void fieldsNotAllowed(List<String> fields, Request userRequest) {
    for (String field : fields) {
      if (((userRequest.getRequest().get(field) instanceof String)
              && StringUtils.isNotBlank((String) userRequest.getRequest().get(field)))
          || (null != userRequest.getRequest().get(field))) {
        throw new ProjectCommonException(
            ResponseCode.invalidRequestParameter.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.invalidRequestParameter.getErrorMessage(), field),
            ERROR_CODE);
      }
    }
  }

  public static void phoneValidation(Request userRequest) {
    if (!StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.COUNTRY_CODE))) {
      boolean bool =
          ProjectUtil.validateCountryCode(
              (String) userRequest.getRequest().get(JsonKey.COUNTRY_CODE));
      if (!bool) {
        throw new ProjectCommonException(
            ResponseCode.invalidCountryCode.getErrorCode(),
            ResponseCode.invalidCountryCode.getErrorMessage(),
            ERROR_CODE);
      }
    }
    if (StringUtils.isNotBlank((String) userRequest.getRequest().get(JsonKey.PHONE))) {
      validatePhoneNo(
          (String) userRequest.getRequest().get(JsonKey.PHONE),
          (String) userRequest.getRequest().get(JsonKey.COUNTRY_CODE));
    }
    if (!StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.PHONE))) {
      if (null != userRequest.getRequest().get(JsonKey.PHONE_VERIFIED)) {
        if (userRequest.getRequest().get(JsonKey.PHONE_VERIFIED) instanceof Boolean) {
          if (!((boolean) userRequest.getRequest().get(JsonKey.PHONE_VERIFIED))) {
            throw new ProjectCommonException(
                ResponseCode.phoneVerifiedError.getErrorCode(),
                ResponseCode.phoneVerifiedError.getErrorMessage(),
                ERROR_CODE);
          }
        } else {
          throw new ProjectCommonException(
              ResponseCode.phoneVerifiedError.getErrorCode(),
              ResponseCode.phoneVerifiedError.getErrorMessage(),
              ERROR_CODE);
        }
      } else {
        throw new ProjectCommonException(
            ResponseCode.phoneVerifiedError.getErrorCode(),
            ResponseCode.phoneVerifiedError.getErrorMessage(),
            ERROR_CODE);
      }
    }
  }

  /**
   * This method will do basic validation for user request object.
   *
   * @param userRequest
   */
  public static void createUserBasicValidation(Request userRequest) {

    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.FIRST_NAME))) {
      throw new ProjectCommonException(
          ResponseCode.firstNameRequired.getErrorCode(),
          ResponseCode.firstNameRequired.getErrorMessage(),
          ERROR_CODE);
    }

    if (userRequest.getRequest().containsKey(JsonKey.ROLES)
        && null != userRequest.getRequest().get(JsonKey.ROLES)
        && !(userRequest.getRequest().get(JsonKey.ROLES) instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.ROLES, JsonKey.LIST),
          ERROR_CODE);
    }
    if (userRequest.getRequest().containsKey(JsonKey.LANGUAGE)
        && null != userRequest.getRequest().get(JsonKey.LANGUAGE)
        && !(userRequest.getRequest().get(JsonKey.LANGUAGE) instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.LANGUAGE, JsonKey.LIST),
          ERROR_CODE);
    }

    if (null != userRequest.getRequest().get(JsonKey.DOB)) {
      boolean bool =
          ProjectUtil.isDateValidFormat(
              ProjectUtil.YEAR_MONTH_DATE_FORMAT,
              (String) userRequest.getRequest().get(JsonKey.DOB));
      if (!bool) {
        throw new ProjectCommonException(
            ResponseCode.dateFormatError.getErrorCode(),
            ResponseCode.dateFormatError.getErrorMessage(),
            ERROR_CODE);
      }
    }

    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EMAIL))
        && StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.PHONE))) {
      throw new ProjectCommonException(
          ResponseCode.emailorPhoneRequired.getErrorCode(),
          ResponseCode.emailorPhoneRequired.getErrorMessage(),
          ERROR_CODE);
    }

    if (!StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EMAIL))
        && !ProjectUtil.isEmailvalid((String) userRequest.getRequest().get(JsonKey.EMAIL))) {
      throw new ProjectCommonException(
          ResponseCode.emailFormatError.getErrorCode(),
          ResponseCode.emailFormatError.getErrorMessage(),
          ERROR_CODE);
    }
  }

  /**
   * Method to validate Address
   *
   * @param userRequest
   */
  @SuppressWarnings("unchecked")
  private static void addressValidation(Request userRequest) {
    Map<String, Object> addrReqMap;
    if (userRequest.getRequest().containsKey(JsonKey.ADDRESS)
        && null != userRequest.getRequest().get(JsonKey.ADDRESS)) {
      if (!(userRequest.getRequest().get(JsonKey.ADDRESS) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.ADDRESS, JsonKey.LIST),
            ERROR_CODE);
      } else if (userRequest.getRequest().get(JsonKey.ADDRESS) instanceof List) {
        List<Map<String, Object>> reqList =
            (List<Map<String, Object>>) userRequest.get(JsonKey.ADDRESS);
        for (int i = 0; i < reqList.size(); i++) {
          addrReqMap = reqList.get(i);
          validateAddress(addrReqMap, JsonKey.ADDRESS);
        }
      }
    }
  }

  /**
   * Method to validate educational details of the user
   *
   * @param userRequest
   */
  @SuppressWarnings("unchecked")
  private static void educationValidation(Request userRequest) {
    Map<String, Object> addrReqMap;
    Map<String, Object> reqMap;
    if (userRequest.getRequest().containsKey(JsonKey.EDUCATION)
        && null != userRequest.getRequest().get(JsonKey.EDUCATION)) {
      if (!(userRequest.getRequest().get(JsonKey.EDUCATION) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.EDUCATION, JsonKey.LIST),
            ERROR_CODE);
      } else if (userRequest.getRequest().get(JsonKey.EDUCATION) instanceof List) {
        List<Map<String, Object>> reqList =
            (List<Map<String, Object>>) userRequest.get(JsonKey.EDUCATION);
        for (int i = 0; i < reqList.size(); i++) {
          reqMap = reqList.get(i);
          if (StringUtils.isBlank((String) reqMap.get(JsonKey.NAME))) {
            throw new ProjectCommonException(
                ResponseCode.educationNameError.getErrorCode(),
                ResponseCode.educationNameError.getErrorMessage(),
                ERROR_CODE);
          }
          if (StringUtils.isBlank((String) reqMap.get(JsonKey.DEGREE))) {
            throw new ProjectCommonException(
                ResponseCode.educationDegreeError.getErrorCode(),
                ResponseCode.educationDegreeError.getErrorMessage(),
                ERROR_CODE);
          }
          if (reqMap.containsKey(JsonKey.ADDRESS) && null != reqMap.get(JsonKey.ADDRESS)) {
            addrReqMap = (Map<String, Object>) reqMap.get(JsonKey.ADDRESS);
            validateAddress(addrReqMap, JsonKey.EDUCATION);
          }
        }
      }
    }
  }

  /**
   * Method to validate jobProfile of a user
   *
   * @param userRequest
   */
  private static void jobProfileValidation(Request userRequest) {
    if (userRequest.getRequest().containsKey(JsonKey.JOB_PROFILE)
        && null != userRequest.getRequest().get(JsonKey.JOB_PROFILE)) {
      if (!(userRequest.getRequest().get(JsonKey.JOB_PROFILE) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.JOB_PROFILE, JsonKey.LIST),
            ERROR_CODE);
      } else if (userRequest.getRequest().get(JsonKey.JOB_PROFILE) instanceof List) {
        validateJob(userRequest);
      }
    }
  }

  private static void validateJob(Request userRequest) {
    Map<String, Object> addrReqMap = null;
    Map<String, Object> reqMap = null;
    List<Map<String, Object>> reqList =
        (List<Map<String, Object>>) userRequest.get(JsonKey.JOB_PROFILE);
    for (int i = 0; i < reqList.size(); i++) {
      reqMap = reqList.get(i);
      if (null != reqMap.get(JsonKey.JOINING_DATE)) {
        boolean bool =
            ProjectUtil.isDateValidFormat(
                ProjectUtil.YEAR_MONTH_DATE_FORMAT, (String) reqMap.get(JsonKey.JOINING_DATE));
        if (!bool) {
          throw new ProjectCommonException(
              ResponseCode.dateFormatError.getErrorCode(),
              ResponseCode.dateFormatError.getErrorMessage(),
              ERROR_CODE);
        }
      }
      if (null != reqMap.get(JsonKey.END_DATE)) {
        boolean bool =
            ProjectUtil.isDateValidFormat(
                ProjectUtil.YEAR_MONTH_DATE_FORMAT, (String) reqMap.get(JsonKey.END_DATE));
        if (!bool) {
          throw new ProjectCommonException(
              ResponseCode.dateFormatError.getErrorCode(),
              ResponseCode.dateFormatError.getErrorMessage(),
              ERROR_CODE);
        }
      }
      if (StringUtils.isBlank((String) reqMap.get(JsonKey.JOB_NAME))) {
        throw new ProjectCommonException(
            ResponseCode.jobNameError.getErrorCode(),
            ResponseCode.jobNameError.getErrorMessage(),
            ERROR_CODE);
      }
      if (StringUtils.isBlank((String) reqMap.get(JsonKey.ORG_NAME))) {
        throw new ProjectCommonException(
            ResponseCode.organisationNameError.getErrorCode(),
            ResponseCode.organisationNameError.getErrorMessage(),
            ERROR_CODE);
      }
      if (reqMap.containsKey(JsonKey.ADDRESS) && null != reqMap.get(JsonKey.ADDRESS)) {
        addrReqMap = (Map<String, Object>) reqMap.get(JsonKey.ADDRESS);
        validateAddress(addrReqMap, JsonKey.JOB_PROFILE);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void validateWebPages(Request request) {
    if (request.getRequest().containsKey(JsonKey.WEB_PAGES)) {
      List<Map<String, String>> data =
          (List<Map<String, String>>) request.getRequest().get(JsonKey.WEB_PAGES);
      if (null == data || data.isEmpty()) {
        throw new ProjectCommonException(
            ResponseCode.invalidWebPageData.getErrorCode(),
            ResponseCode.invalidWebPageData.getErrorMessage(),
            ERROR_CODE);
      }
    }
  }

  private static void validateAddress(Map<String, Object> address, String type) {
    if (StringUtils.isBlank((String) address.get(JsonKey.ADDRESS_LINE1))) {
      throw new ProjectCommonException(
          ResponseCode.addressError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.addressError.getErrorMessage(), type, JsonKey.ADDRESS_LINE1),
          ERROR_CODE);
    }
    if (StringUtils.isBlank((String) address.get(JsonKey.CITY))) {
      throw new ProjectCommonException(
          ResponseCode.addressError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.addressError.getErrorMessage(), type, JsonKey.CITY),
          ERROR_CODE);
    }
    if (address.containsKey(JsonKey.ADD_TYPE) && type.equals(JsonKey.ADDRESS)) {

      if (StringUtils.isBlank((String) address.get(JsonKey.ADD_TYPE))) {
        throw new ProjectCommonException(
            ResponseCode.addressError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.addressError.getErrorMessage(), type, JsonKey.TYPE),
            ERROR_CODE);
      }

      if (!StringUtils.isBlank((String) address.get(JsonKey.ADD_TYPE))
          && !checkAddressType((String) address.get(JsonKey.ADD_TYPE))) {
        throw new ProjectCommonException(
            ResponseCode.addressTypeError.getErrorCode(),
            ResponseCode.addressTypeError.getErrorMessage(),
            ERROR_CODE);
      }
    }
  }

  private static boolean checkAddressType(String addrType) {
    for (AddressType type : AddressType.values()) {
      if (type.getTypeName().equals(addrType)) {
        return true;
      }
    }
    return false;
  }

  private static boolean validatePhoneNo(String phone, String countryCode) {
    if (phone.contains("+")) {
      throw new ProjectCommonException(
          ResponseCode.invalidPhoneNumber.getErrorCode(),
          ResponseCode.invalidPhoneNumber.getErrorMessage(),
          ERROR_CODE);
    }
    if (ProjectUtil.validatePhone(phone, countryCode)) {
      return true;
    } else {
      throw new ProjectCommonException(
          ResponseCode.phoneNoFormatError.getErrorCode(),
          ResponseCode.phoneNoFormatError.getErrorMessage(),
          ERROR_CODE);
    }
  }

  /**
   * This method will validate update user data.
   *
   * @param userRequest Request
   */
  @SuppressWarnings({"rawtypes"})
  public static void validateUpdateUser(Request userRequest) {
    externalIdsValidation(userRequest, JsonKey.UPDATE);
    phoneValidation(userRequest);
    updateUserBasicValidation(userRequest);
    if (userRequest.getRequest().get(JsonKey.ADDRESS) != null
        && ((List) userRequest.getRequest().get(JsonKey.ADDRESS)).isEmpty()) {
      throw new ProjectCommonException(
          ResponseCode.addressRequired.getErrorCode(),
          ResponseCode.addressRequired.getErrorMessage(),
          ERROR_CODE);
    }
    if (userRequest.getRequest().get(JsonKey.EDUCATION) != null
        && ((List) userRequest.getRequest().get(JsonKey.EDUCATION)).isEmpty()) {
      throw new ProjectCommonException(
          ResponseCode.educationRequired.getErrorCode(),
          ResponseCode.educationRequired.getErrorMessage(),
          ERROR_CODE);
    }
    if (userRequest.getRequest().get(JsonKey.JOB_PROFILE) != null
        && ((List) userRequest.getRequest().get(JsonKey.JOB_PROFILE)).isEmpty()) {
      throw new ProjectCommonException(
          ResponseCode.jobDetailsRequired.getErrorCode(),
          ResponseCode.jobDetailsRequired.getErrorMessage(),
          ERROR_CODE);
    }

    if (userRequest.getRequest().get(JsonKey.ADDRESS) != null
        && (!((List) userRequest.getRequest().get(JsonKey.ADDRESS)).isEmpty())) {
      validateUpdateUserAddress(userRequest);
    }

    if (userRequest.getRequest().get(JsonKey.JOB_PROFILE) != null
        && (!((List) userRequest.getRequest().get(JsonKey.JOB_PROFILE)).isEmpty())) {
      validateUpdateUserJobProfile(userRequest);
    }
    if (userRequest.getRequest().get(JsonKey.EDUCATION) != null
        && (!((List) userRequest.getRequest().get(JsonKey.EDUCATION)).isEmpty())) {
      validateUpdateUserEducation(userRequest);
    }
    if (userRequest.getRequest().containsKey(JsonKey.ROOT_ORG_ID)
        && StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.ROOT_ORG_ID))) {
      throw new ProjectCommonException(
          ResponseCode.invalidRootOrganisationId.getErrorCode(),
          ResponseCode.invalidRootOrganisationId.getErrorMessage(),
          ERROR_CODE);
    }

    validateExtIdTypeAndProvider(userRequest);
  }

  public static void externalIdsValidation(Request userRequest, String operation) {
    if (userRequest.getRequest().containsKey(JsonKey.EXTERNAL_IDS)
        && (null != userRequest.getRequest().get(JsonKey.EXTERNAL_IDS))) {
      if (!(userRequest.getRequest().get(JsonKey.EXTERNAL_IDS) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.EXTERNAL_IDS, JsonKey.LIST),
            ERROR_CODE);
      }
      List<Map<String, String>> externalIds =
          (List<Map<String, String>>) userRequest.getRequest().get(JsonKey.EXTERNAL_IDS);
      validateIndividualExternalId(operation, externalIds);
      if (operation.equalsIgnoreCase(JsonKey.CREATE)) {
        checkForDuplicateExternalId(externalIds);
      }
    }
  }

  private static void validateIndividualExternalId(
      String operation, List<Map<String, String>> externalIds) {
    // valid operation type for externalIds in user api.
    List<String> operationTypeList = Arrays.asList(JsonKey.ADD, JsonKey.REMOVE, JsonKey.EDIT);
    externalIds
        .stream()
        .forEach(
            identity -> {
              // check for invalid operation type
              if (StringUtils.isNotBlank(identity.get(JsonKey.OPERATION))
                  && (!operationTypeList.contains(
                      (identity.get(JsonKey.OPERATION)).toLowerCase()))) {
                throw new ProjectCommonException(
                    ResponseCode.invalidValue.getErrorCode(),
                    ProjectUtil.formatMessage(
                        ResponseCode.invalidValue.getErrorMessage(),
                        StringFormatter.joinByDot(JsonKey.EXTERNAL_IDS, JsonKey.OPERATION),
                        identity.get(JsonKey.OPERATION),
                        String.join(StringFormatter.COMMA, operationTypeList)),
                    ERROR_CODE);
              }
              // throw exception for invalid operation if other operation type is coming in
              // request
              // other than add or null for create user api
              if (JsonKey.CREATE.equalsIgnoreCase(operation)
                  && StringUtils.isNotBlank(identity.get(JsonKey.OPERATION))
                  && (!JsonKey.ADD.equalsIgnoreCase(((identity.get(JsonKey.OPERATION)))))) {
                throw new ProjectCommonException(
                    ResponseCode.invalidValue.getErrorCode(),
                    ProjectUtil.formatMessage(
                        ResponseCode.invalidValue.getErrorMessage(),
                        StringFormatter.joinByDot(JsonKey.EXTERNAL_IDS, JsonKey.OPERATION),
                        identity.get(JsonKey.OPERATION),
                        JsonKey.ADD),
                    ERROR_CODE);
              }
              validateExternalIdMandatoryParam(JsonKey.ID, identity.get(JsonKey.ID));
              validateExternalIdMandatoryParam(JsonKey.PROVIDER, identity.get(JsonKey.PROVIDER));
              validateExternalIdMandatoryParam(JsonKey.ID_TYPE, identity.get(JsonKey.ID_TYPE));
            });
  }

  private static void validateExternalIdMandatoryParam(String param, String paramValue) {
    if (StringUtils.isBlank(paramValue)) {
      throw new ProjectCommonException(
          ResponseCode.mandatoryParamsMissing.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.mandatoryParamsMissing.getErrorMessage(),
              StringFormatter.joinByDot(JsonKey.EXTERNAL_IDS, param)),
          ERROR_CODE);
    }
  }

  private static void validateUpdateUserEducation(Request userRequest) {
    List<Map<String, Object>> reqList =
        (List<Map<String, Object>>) userRequest.get(JsonKey.EDUCATION);
    for (int i = 0; i < reqList.size(); i++) {
      Map<String, Object> reqMap = reqList.get(i);
      if (reqMap.containsKey(JsonKey.IS_DELETED)
          && null != reqMap.get(JsonKey.IS_DELETED)
          && ((boolean) reqMap.get(JsonKey.IS_DELETED))
          && StringUtils.isBlank((String) reqMap.get(JsonKey.ID))) {
        throw new ProjectCommonException(
            ResponseCode.idRequired.getErrorCode(),
            ResponseCode.idRequired.getErrorMessage(),
            ERROR_CODE);
      }
      if (!reqMap.containsKey(JsonKey.IS_DELETED)
          || (reqMap.containsKey(JsonKey.IS_DELETED)
              && (null == reqMap.get(JsonKey.IS_DELETED)
                  || !(boolean) reqMap.get(JsonKey.IS_DELETED)))) {
        educationValidation(userRequest);
      }
    }
  }

  private static void validateUpdateUserJobProfile(Request userRequest) {
    List<Map<String, Object>> reqList =
        (List<Map<String, Object>>) userRequest.get(JsonKey.JOB_PROFILE);
    for (int i = 0; i < reqList.size(); i++) {
      Map<String, Object> reqMap = reqList.get(i);
      if (reqMap.containsKey(JsonKey.IS_DELETED)
          && null != reqMap.get(JsonKey.IS_DELETED)
          && ((boolean) reqMap.get(JsonKey.IS_DELETED))
          && StringUtils.isBlank((String) reqMap.get(JsonKey.ID))) {
        throw new ProjectCommonException(
            ResponseCode.idRequired.getErrorCode(),
            ResponseCode.idRequired.getErrorMessage(),
            ERROR_CODE);
      }
      if (!reqMap.containsKey(JsonKey.IS_DELETED)
          || (reqMap.containsKey(JsonKey.IS_DELETED)
              && (null == reqMap.get(JsonKey.IS_DELETED)
                  || !(boolean) reqMap.get(JsonKey.IS_DELETED)))) {
        jobProfileValidation(userRequest);
      }
    }
  }

  private static void validateUpdateUserAddress(Request userRequest) {
    List<Map<String, Object>> reqList =
        (List<Map<String, Object>>) userRequest.get(JsonKey.ADDRESS);
    for (int i = 0; i < reqList.size(); i++) {
      Map<String, Object> reqMap = reqList.get(i);

      if (reqMap.containsKey(JsonKey.IS_DELETED)
          && null != reqMap.get(JsonKey.IS_DELETED)
          && ((boolean) reqMap.get(JsonKey.IS_DELETED))
          && StringUtils.isBlank((String) reqMap.get(JsonKey.ID))) {
        throw new ProjectCommonException(
            ResponseCode.idRequired.getErrorCode(),
            ResponseCode.idRequired.getErrorMessage(),
            ERROR_CODE);
      }
      if (!reqMap.containsKey(JsonKey.IS_DELETED)
          || (reqMap.containsKey(JsonKey.IS_DELETED)
              && (null == reqMap.get(JsonKey.IS_DELETED)
                  || !(boolean) reqMap.get(JsonKey.IS_DELETED)))) {
        validateAddress(reqMap, JsonKey.ADDRESS);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private static void updateUserBasicValidation(Request userRequest) {
    fieldsNotAllowed(
        Arrays.asList(
            JsonKey.REGISTERED_ORG_ID,
            JsonKey.ROOT_ORG_ID,
            JsonKey.CHANNEL,
            JsonKey.USERNAME,
            JsonKey.PROVIDER,
            JsonKey.ID_TYPE),
        userRequest);
    if ((StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.USER_ID))
            && StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.ID)))
        && (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID))
            || StringUtils.isBlank(
                (String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_PROVIDER))
            || StringUtils.isBlank(
                (String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_TYPE)))) {
      throw new ProjectCommonException(
          ResponseCode.mandatoryParamsMissing.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.mandatoryParamsMissing.getErrorMessage(),
              (StringFormatter.joinByOr(
                  JsonKey.USER_ID,
                  StringFormatter.joinByAnd(
                      StringFormatter.joinByComma(JsonKey.EXTERNAL_ID, JsonKey.EXTERNAL_ID_TYPE),
                      JsonKey.EXTERNAL_ID_PROVIDER)))),
          ERROR_CODE);
    }
    if (userRequest.getRequest().containsKey(JsonKey.FIRST_NAME)
        && (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.FIRST_NAME)))) {
      throw new ProjectCommonException(
          ResponseCode.firstNameRequired.getErrorCode(),
          ResponseCode.firstNameRequired.getErrorMessage(),
          ERROR_CODE);
    }

    if ((userRequest.getRequest().containsKey(JsonKey.EMAIL)
            && userRequest.getRequest().get(JsonKey.EMAIL) != null)
        && !ProjectUtil.isEmailvalid((String) userRequest.getRequest().get(JsonKey.EMAIL))) {
      throw new ProjectCommonException(
          ResponseCode.emailFormatError.getErrorCode(),
          ResponseCode.emailFormatError.getErrorMessage(),
          ERROR_CODE);
    }

    if (userRequest.getRequest().containsKey(JsonKey.ROLES)
        && null != userRequest.getRequest().get(JsonKey.ROLES)) {
      if (userRequest.getRequest().get(JsonKey.ROLES) instanceof List
          && ((List) userRequest.getRequest().get(JsonKey.ROLES)).isEmpty()) {
        throw new ProjectCommonException(
            ResponseCode.rolesRequired.getErrorCode(),
            ResponseCode.rolesRequired.getErrorMessage(),
            ERROR_CODE);
      } else if (!(userRequest.getRequest().get(JsonKey.ROLES) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.ROLES, JsonKey.LIST),
            ERROR_CODE);
      }
    }
    if (userRequest.getRequest().containsKey(JsonKey.LANGUAGE)
        && null != userRequest.getRequest().get(JsonKey.LANGUAGE)) {
      if (userRequest.getRequest().get(JsonKey.LANGUAGE) instanceof List
          && ((List) userRequest.getRequest().get(JsonKey.LANGUAGE)).isEmpty()) {
        throw new ProjectCommonException(
            ResponseCode.languageRequired.getErrorCode(),
            ResponseCode.languageRequired.getErrorMessage(),
            ERROR_CODE);
      } else if (!(userRequest.getRequest().get(JsonKey.LANGUAGE) instanceof List)) {
        throw new ProjectCommonException(
            ResponseCode.dataTypeError.getErrorCode(),
            ProjectUtil.formatMessage(
                ResponseCode.dataTypeError.getErrorMessage(), JsonKey.LANGUAGE, JsonKey.LIST),
            ERROR_CODE);
      }
    }
  }

  /**
   * This method will validate change password requested data.
   *
   * @param userRequest Request
   */
  public static void validateChangePassword(Request userRequest) {
    if (userRequest.getRequest().get(JsonKey.PASSWORD) == null
        || (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.PASSWORD)))) {
      throw new ProjectCommonException(
          ResponseCode.passwordRequired.getErrorCode(),
          ResponseCode.passwordRequired.getErrorMessage(),
          ERROR_CODE);
    }
    if (userRequest.getRequest().get(JsonKey.NEW_PASSWORD) == null) {
      throw new ProjectCommonException(
          ResponseCode.newPasswordRequired.getErrorCode(),
          ResponseCode.newPasswordRequired.getErrorMessage(),
          ERROR_CODE);
    }
    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.NEW_PASSWORD))) {
      throw new ProjectCommonException(
          ResponseCode.newPasswordEmpty.getErrorCode(),
          ResponseCode.newPasswordEmpty.getErrorMessage(),
          ERROR_CODE);
    }
  }

  /**
   * This method will validate verifyUser requested data.
   *
   * @param userRequest Request
   */
  public static void validateVerifyUser(Request userRequest) {
    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.LOGIN_ID))) {
      throw new ProjectCommonException(
          ResponseCode.loginIdRequired.getErrorCode(),
          ResponseCode.loginIdRequired.getErrorMessage(),
          ERROR_CODE);
    }
  }

  /**
   * Either user will send UserId or (provider and externalId).
   *
   * @param request
   */
  public static void validateAssignRole(Request request) {
    if (StringUtils.isBlank((String) request.getRequest().get(JsonKey.USER_ID))) {
      throw new ProjectCommonException(
          ResponseCode.userIdRequired.getErrorCode(),
          ResponseCode.userIdRequired.getErrorMessage(),
          ERROR_CODE);
    }

    if (request.getRequest().get(JsonKey.ROLES) == null
        || !(request.getRequest().get(JsonKey.ROLES) instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.ROLES, JsonKey.LIST),
          ERROR_CODE);
    }

    String organisationId = (String) request.getRequest().get(JsonKey.ORGANISATION_ID);
    String externalId = (String) request.getRequest().get(JsonKey.EXTERNAL_ID);
    String provider = (String) request.getRequest().get(JsonKey.PROVIDER);
    if (StringUtils.isBlank(organisationId)
        && (StringUtils.isBlank(externalId) || StringUtils.isBlank(provider))) {
      throw new ProjectCommonException(
          ResponseCode.mandatoryParamsMissing.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.mandatoryParamsMissing.getErrorMessage(),
              (StringFormatter.joinByOr(
                  JsonKey.ORGANISATION_ID,
                  StringFormatter.joinByAnd(JsonKey.EXTERNAL_ID, JsonKey.PROVIDER)))),
          ERROR_CODE);
    }
  }

  /** @param request */
  public static void validateForgotpassword(Request request) {
    if (request.getRequest().get(JsonKey.USERNAME) == null
        || StringUtils.isBlank((String) request.getRequest().get(JsonKey.USERNAME))) {
      throw new ProjectCommonException(
          ResponseCode.userNameRequired.getErrorCode(),
          ResponseCode.userNameRequired.getErrorMessage(),
          ERROR_CODE);
    }
  }

  @SuppressWarnings("unchecked")
  public static void validateProfileVisibility(Request request) {
    if (request.getRequest().get(JsonKey.PRIVATE) == null
        && request.getRequest().get(JsonKey.PUBLIC) == null) {
      throw new ProjectCommonException(
          ResponseCode.invalidData.getErrorCode(),
          ResponseCode.invalidData.getErrorMessage(),
          ERROR_CODE);
    }
    if (request.getRequest().containsKey(JsonKey.PRIVATE)
        && !(request.getRequest().get(JsonKey.PRIVATE) instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.PRIVATE, JsonKey.LIST),
          ERROR_CODE);
    }
    if (request.getRequest().containsKey(JsonKey.PUBLIC)
        && !(request.getRequest().get(JsonKey.PUBLIC) instanceof List)) {
      throw new ProjectCommonException(
          ResponseCode.dataTypeError.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dataTypeError.getErrorMessage(), JsonKey.PUBLIC, JsonKey.LIST),
          ERROR_CODE);
    }
    if (request.getRequest().get(JsonKey.USER_ID) == null
        || StringUtils.isBlank(((String) request.getRequest().get(JsonKey.USER_ID)))) {
      throw new ProjectCommonException(
          ResponseCode.usernameOrUserIdError.getErrorCode(),
          ResponseCode.usernameOrUserIdError.getErrorMessage(),
          ERROR_CODE);
    }
    if (null != request.getRequest().get(JsonKey.PRIVATE)
        && null != request.getRequest().get(JsonKey.PUBLIC)) {
      List<String> privateList = (List<String>) request.getRequest().get(JsonKey.PRIVATE);
      List<String> publicList = (List<String>) request.getRequest().get(JsonKey.PUBLIC);
      if (privateList.size() > publicList.size()) {
        for (String field : publicList) {
          if (privateList.contains(field)) {
            throw new ProjectCommonException(
                ResponseCode.visibilityInvalid.getErrorCode(),
                ResponseCode.visibilityInvalid.getErrorMessage(),
                ERROR_CODE);
          }
        }
      } else {
        for (String field : privateList) {
          if (publicList.contains(field)) {
            throw new ProjectCommonException(
                ResponseCode.visibilityInvalid.getErrorCode(),
                ResponseCode.visibilityInvalid.getErrorMessage(),
                ERROR_CODE);
          }
        }
      }
    }
  }

  /**
   * This method will validate bulk api user data.
   *
   * @param userRequest Request
   */
  public static void validateBulkUserData(Request userRequest) {
    externalIdsValidation(userRequest, JsonKey.BULK_USER_UPLOAD);
    createUserBasicValidation(userRequest);
    phoneValidation(userRequest);
    validateWebPages(userRequest);
    validateExtIdTypeAndProvider(userRequest);
    if (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.USERNAME))
        && (StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_PROVIDER))
            || StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID))
            || StringUtils.isBlank(
                (String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_TYPE)))) {
      throw new ProjectCommonException(
          ResponseCode.mandatoryParamsMissing.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.mandatoryParamsMissing.getErrorMessage(),
              (StringFormatter.joinByOr(
                  JsonKey.USERNAME,
                  StringFormatter.joinByAnd(
                      StringFormatter.joinByComma(JsonKey.EXTERNAL_ID, JsonKey.EXTERNAL_ID_TYPE),
                      JsonKey.EXTERNAL_ID_PROVIDER)))),
          ERROR_CODE);
    }
  }

  private static void validateExtIdTypeAndProvider(Request userRequest) {
    if ((StringUtils.isNotBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_PROVIDER))
        && StringUtils.isNotBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID))
        && StringUtils.isNotBlank(
            (String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_TYPE)))) {
      return;
    } else if (StringUtils.isBlank(
            (String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_PROVIDER))
        && StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID))
        && StringUtils.isBlank((String) userRequest.getRequest().get(JsonKey.EXTERNAL_ID_TYPE))) {
      return;
    } else {
      throw new ProjectCommonException(
          ResponseCode.dependentParamsMissing.getErrorCode(),
          ProjectUtil.formatMessage(
              ResponseCode.dependentParamsMissing.getErrorMessage(),
              StringFormatter.joinByComma(
                  JsonKey.EXTERNAL_ID, JsonKey.EXTERNAL_ID_TYPE, JsonKey.EXTERNAL_ID_PROVIDER)),
          ERROR_CODE);
    }
  }

  private static void checkForDuplicateExternalId(List<Map<String, String>> list) {
    List<Map<String, String>> checkedList = new ArrayList<>();
    for (Map<String, String> externalId : list) {
      for (Map<String, String> checkedExternalId : checkedList) {
        String provider = checkedExternalId.get(JsonKey.PROVIDER);
        String idType = checkedExternalId.get(JsonKey.ID_TYPE);
        if (provider.equalsIgnoreCase(externalId.get(JsonKey.PROVIDER))
            && idType.equalsIgnoreCase(externalId.get(JsonKey.ID_TYPE))) {
          String exceptionMsg =
              MessageFormat.format(
                  ResponseCode.duplicateExternalIds.getErrorMessage(), idType, provider);
          ProjectCommonException.throwClientErrorException(
              ResponseCode.duplicateExternalIds, exceptionMsg);
        }
      }
      checkedList.add(externalId);
    }
  }
}
