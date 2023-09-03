export class Package {
        id: String;
        package_name: String;
        bin_version: String;
        app_version: String;
        mcu_version: String;
        ble_version: String;
        config1: String;
        config2: String;
        config3: String;
        config4: String;
        config5 : String;
      config1_crc : String;
      config2_crc : String;
      config3_crc : String;
      config4_crc : String;
      config5_crc : String;
      is_used_in_campaign = Boolean;

      device_type : String;
      lite_sentry_hardware : String;
      lite_sentry_app : String;
      lite_sentry_boot : String;
      microsp_app : String;
      microsp_mcu : String;
      cargo_maxbotix_hardware : String;
      cargo_maxbotix_firmware : String;
      cargo_riot_hardware : String;
      cargo_riot_firmware : String;
}