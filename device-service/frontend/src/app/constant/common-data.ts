export class CommonData {

    static readonly DEVICE_FORWORDING_RULE_DATA: Array<any> = [
        { id: 1, value: 'elastic', data: 'Elastic (RAW)' },
        { id: 2, value: 'json', data: 'Standard JSON' },
        { id: 3, value: 'ups', data: 'Custom (UPS)' },
        { id: 4, value: 'eroad', data: 'Kinesis (EROAD)' },
        { id: 5, value: 'kafka', data: 'Kafka (RAW)' }
    ];

    static readonly DATA_FORWORDING_RULE_FORMAT_MAPPING: any = {
        'Standard JSON': 'json',
        'Elastic (RAW)': 'elastic',
        'Custom (UPS)': 'ups',
        'Kinesis (EROAD)': 'eroad',
        'kafka (Amazon)': 'kafka'
    };

    static readonly DATE_RANGE_DATA: Array<any> = [
        { id: 1, value: '', data: 'All' },
        { id: 2, value: 'last_hour', data: 'Last hour' },
        { id: 3, value: 'last_24_hour', data: 'Last 24 hours' },
        { id: 4, value: 'last_week', data: 'Last week' },
        { id: 5, value: 'last_month', data: 'Last month' },
        { id: 6, value: 'last_year', data: 'Last year' },
        { id: 7, value: 'time_rang', data: 'Specify date/time range' }
    ];

    static readonly DEVICE_TYPE_DATA: Array<any> = [
        { id: 1, value: '', data: 'All' },
        { id: 2, value: 'BEACON', data: 'BEACON' },
        { id: 3, value: 'GATEWAY', data: 'GATEWAY' },
        { id: 4, value: 'SENSOR', data: 'SENSOR   ' }
    ];

    static readonly CAMPAIGN_DEVICE_TYPE_DATA: Array<any> = [
        { id: 1, value: '', data: 'All' },
        { id: 2, value: 'freight-la', data: 'freight-la' },
        { id: 3, value: 'arrow-l', data: 'arrow-l' },
        { id: 4, value: 'freight-l', data: 'freight-l' },
        { id: 5, value: 'cutlass-l', data: 'cutlass-l' },
        { id: 6, value: 'Smart7', data: 'Smart7' },
        { id: 7, value: 'TrailerNet', data: 'TrailerNet' },
        { id: 8, value: 'StealthNet', data: 'StealthNet' },
        { id: 9, value: 'chassisNet', data: 'chassisNet' },
    ];

    static readonly USAGE_DATA: Array<any> = [
        { id: 1, value: 'production', data: 'Production' },
        { id: 2, value: 'demo', data: 'Demo' },
        { id: 3, value: 'rma', data: 'RMA' },
        { id: 4, value: 'pilot', data: 'Pilot' },
        { id: 5, value: 'eol', data: 'EOL' },
        { id: 6, value: 'engineering', data: 'Engineering' },
        { id: 7, value: 'unknown', data: 'unknown' },
        { id: 8, value: 'installed', data: 'Installed' }
    ];

    static readonly SENSOR_TYPE_MAP: any = new Map([
        ['Lite Sentry', 'lite_sentry'],
        ['BLE PCT Door Sensor', 'ble_door_sensor'],
        ['Lamp Check ATIS', 'alpha_atis'],
        ['Chassis', 'chassis'],
        ['MicroSP TPMS Sensor', 'tpms_alpha'],
        ['MICROSP TPMS SENSOR', 'tpms_alpha'],
        ['Alpha ABS', 'abs'],
        ['Smart ABS Harness (Includes ABS Fault Reader)', ''],
        ['PCT Cargo Sensor', ''],
        ['Door Sensor (Wireless)', 'door_sensor'],
        ['Door Sensor', 'door_sensor'],
        ['MicroSP Wired Receiver', ''],
        ['MicroSP Air Tank', 'psi_air_supply'],
        ['MicroSP ATIS Regulator', '']
    ]);

    static readonly SENSOR_STATUS_KEY_VALUE_MAP: any = new Map([
        ['lite_sentry', 'communicating'],
        ['ble_door_sensor', 'doorState'],
        ['alpha_atis', 'condition'],
        ['chassis', 'cargo_state'],
        ['tpms_alpha', 'tpmsMeasures'],
        ['abs', 'notPoweredUp'],
    ]);

    static readonly TELECOM_PROVIDER_DATA: Array<any> = [
        { id: 1, value: 'Jersey Telecom', data: 'Jersey Telecom' },
        { id: 2, value: 'T-Mobile', data: 'T-Mobile' },
        { id: 3, value: 'Singtel', data: 'Singtel' },
        { id: 4, value: 'Movistar', data: 'Movistar' },
        { id: 5, value: 'AT&T Wireless Inc.', data: 'AT&T Wireless Inc.' },
        { id: 6, value: '690', data: '690' },
        { id: 7, value: 'Airtel/Vodafone', data: 'Airtel/Vodafone' },
        { id: 8, value: 'jersey telco', data: 'jersey telco' },
        { id: 9, value: '50', data: '50' },
        { id: 10, value: '70', data: '70' },
        { id: 11, value: 'AMC/Cosmote', data: 'AMC/Cosmote' },
        { id: 12, value: 'Aeris Comm. Inc.', data: 'Aeris Comm. Inc.' },
        { id: 13, value: 'Verizon Wireless', data: 'Verizon Wireless' },
        { id: 14, value: 'Vodafone Libertel', data: 'Vodafone Libertel' },
        { id: 15, value: 'Jersy Network', data: 'Jersy Network' },
    ];


    static readonly SENSOR_LIST: Array<any> = [
        {
            sensor_uuid: null,
            product_code: '77-S137',
            product_name: 'Air Tank Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-S120',
            product_name: 'ATIS + Hardware Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-S102',
            product_name: 'Cargo Sensor (Wireless)'
        },
        {
            sensor_uuid: null,
            product_code: '77-S108',
            product_name: 'Door Sensor (Wireless)'
        },
        {
            sensor_uuid: null,
            product_code: '77-S191',
            product_name: 'LampCheck ABS'
        },
        {
            sensor_uuid: null,
            product_code: '77-S188',
            product_name: 'LampCheck ATIS'
        },
        {
            sensor_uuid: null,
            product_code: '77-S107',
            product_name: 'Lite Sentry'
        },
        {
            sensor_uuid: null,
            product_code: '77-SXXX',
            product_name: 'Maxon MaxLink'
        },
        {
            sensor_uuid: null,
            product_code: '77-S202',
            product_name: 'MicroSP Air Tank'
        },
        {
            sensor_uuid: null,
            product_code: '77-S203',
            product_name: 'MicroSP ATIS Regulator'
        },
        {
            sensor_uuid: null,
            product_code: '77-S198',
            product_name: 'MicroSP Flowthrough TPMS (Inner)'
        },
        {
            sensor_uuid: null,
            product_code: '77-S199',
            product_name: 'MicroSP Flowthrough TPMS (Outer)'
        },
        {
            sensor_uuid: null,
            product_code: '77-S177',
            product_name: 'MicroSP TPMS Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-S206',
            product_name: 'MicroSP Wired Receiver'
        },
        {
            sensor_uuid: null,
            product_code: '77-S196',
            product_name: 'MicroSP Wireless Receiver'
        },
        {
            sensor_uuid: null,
            product_code: '77-S111',
            product_name: 'PCT Cargo Camera'
        },
        {
            sensor_uuid: null,
            product_code: '77-S225',
            product_name: 'PCT Cargo Camera G2'
        },
        {
            sensor_uuid: null,
            product_code: '77-S226',
            product_name: 'PCT Cargo Camera G3'
        },
        {
            sensor_uuid: null,
            product_code: '77-S180',
            product_name: 'PCT Cargo Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-S500',
            product_name: 'PSI TireView - 8 Tires Excluding CP Hoses'
        },
        {
            sensor_uuid: null,
            product_code: '77-S164',
            product_name: 'Regulator Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-H101',
            product_name: 'Smart ABS Harness (Includes ABS Fault Reader)'
        },
        {
            sensor_uuid: null,
            product_code: '77-S119',
            product_name: 'Wheel End Sensor'
        },
        {
            sensor_uuid: null,
            product_code: '77-S183',
            product_name: 'Wheel End Sensor'
        }
    ];

    static readonly SENSOR_SECTION_LIST: Array<any> = [
        { section_name: 'Amazon', ection_id: '30300e47-b305-441d-9577-18436479a241' }
    ];

    static readonly PAGE_SIZE_DATA: Array<any> = [
        { id: 1, value: '10', data: '10' },
        { id: 2, value: '20', data: '20' },
        { id: 3, value: '25', data: '25' },
        { id: 4, value: '30', data: '30' },
        { id: 5, value: '40', data: '40' },
        { id: 6, value: '50', data: '50' },
        { id: 7, value: '100', data: '100' },
        { id: 8, value: '150', data: '150' },
        { id: 9, value: '200', data: '200' },
        { id: 10, value: '300', data: '300' },
        { id: 11, value: '500', data: '500' }
    ];

    static readonly SENSOR_POSITION: Array<any> = [
        { id: 1, name: 'Left Outer Front', value: '0x21', data: 'FLO', altName: 'Pending' },
        { id: 2, name: 'Left Inner Front', value: '0x22', data: 'FLI', altName: 'Pending' },
        { id: 3, name: 'Right Inner Front', value: '0x23', data: 'FRI', altName: 'Pending' },
        { id: 4, name: 'Right Outer Front', value: '0x24', data: 'FRO', altName: 'Pending' },
        { id: 5, name: 'Left Outer Rear', value: '0x25', data: 'RLO', altName: 'Pending' },
        { id: 6, name: 'Left Inner Rear', value: '0x26', data: 'RLI', altName: 'Pending' },
        { id: 7, name: 'Rght Inner Rear', value: '0x27', data: 'RRI', altName: 'Pending' },
        { id: 8, name: 'Right Outer Rear', value: '0x28', data: 'RRO', altName: 'Pending' },
        { id: 9, name: 'ATIS Regulatorr', value: '0x4A', data: 'ATIS Regulator', altName: '--' },
        { id: 10, name: 'Air Tank', value: '0x49', data: 'Air Tank', altName: '--' },
        { id: 11, name: 'Left Front', value: '0x41', data: 'Left Front', altName: '--' },
        { id: 12, name: 'Right Front', value: '0x42', data: 'Right Front', altName: '--' },
        { id: 13, name: 'Left Rear', value: '0x43', data: 'Left Rear', altName: '--' },
        { id: 14, name: 'Right Rear', value: '0x44', data: 'Right Rear', altName: '--' },
    ];


    static readonly DEVICE_LIST_COLUMN_FOR_EXPORT: Array<any> = [
        { id: 1, dbName: 'created_by', headerName: 'Customer' },
        { id: 1, dbName: 'forwarding_group', headerName: 'ForwardingGroup' },
        { id: 1, dbName: 'purchase_by_name', headerName: 'PurchaseByName' },
        { id: 1, dbName: 'imei', headerName: 'DeviceId' },
        { id: 1, dbName: 'asset_name', headerName: 'AssetName' },
        { id: 1, dbName: 'asset_type', headerName: 'AssetType' },
        { id: 1, dbName: 'installedDateTimestamp', headerName: 'InstalledDate' },
        { id: 1, dbName: 'vin', headerName: 'Vin' },
        { id: 1, dbName: 'manufacturer', headerName: 'Manufacturer' },
        { id: 1, dbName: 'status', headerName: 'Status' },
        { id: 1, dbName: 'son', headerName: 'Order' },
        { id: 1, dbName: 'device_type', headerName: 'Model' },
        { id: 1, dbName: 'product_name', headerName: 'ProductName' },
        { id: 1, dbName: 'battery', headerName: 'Battery' },
        { id: 1, dbName: 'installation_date', headerName: 'InstallationDate' },
        { id: 1, dbName: 'last_report', headerName: 'LastReportDate' },
        { id: 1, dbName: 'last_report_date_time', headerName: 'LastReportDateTime' },
        { id: 1, dbName: 'event_id', headerName: 'EventId' },
        { id: 1, dbName: 'event_type', headerName: 'EventType' },
        { id: 1, dbName: 'lat', headerName: 'Lat' },
        { id: 1, dbName: 'longitude', headerName: 'Longitude' },
        { id: 1, dbName: 'qa_status', headerName: 'QaStatus' },
        { id: 1, dbName: 'qa_date', headerName: 'QaDates' },
        { id: 1, dbName: 'config_name', headerName: 'ConfigName' },
        { id: 1, dbName: 'usage_status', headerName: 'UsageStatus' },
        { id: 1, dbName: 'imei_hashed', headerName: 'SecStatus' },
        { id: 1, dbName: 'revoked_time', headerName: 'SecDate' },
        { id: 1, dbName: 'service_network', headerName: 'ServiceNetwork' },
        { id: 1, dbName: 'cellular', headerName: 'Sim' },
        { id: 1, dbName: 'service_country', headerName: 'ServiceCountry' },
        { id: 1, dbName: 'phone', headerName: 'Phone' },
        { id: 1, dbName: 'rule1', headerName: 'Rule1' },
        { id: 1, dbName: 'rule1_source', headerName: 'Rule1Source' },
        { id: 1, dbName: 'rule1', headerName: 'Rule2' },
        { id: 1, dbName: 'rule2_source', headerName: 'Rule2Source' },
        { id: 1, dbName: 'rule3', headerName: 'Rule3' },
        { id: 1, dbName: 'rule3_source', headerName: 'Rule3Source' },
        { id: 1, dbName: 'rule4', headerName: 'Rule4' },
        { id: 1, dbName: 'rule4_source', headerName: 'Rule4Source' },
        { id: 1, dbName: 'rule5', headerName: 'Rule5' },
        { id: 1, dbName: 'rule5_source', headerName: 'Rule5Source' },
        { id: 1, dbName: 'bin_version', headerName: 'BinVersion' },
        { id: 1, dbName: 'app_version', headerName: 'AppVersion' },
        { id: 1, dbName: 'mcu_version', headerName: 'McuVersion' },
        { id: 1, dbName: 'ble_version', headerName: 'BleVersion' },
        { id: 1, dbName: 'config1_name', headerName: 'Config1Name' },
        { id: 1, dbName: 'config1_crc', headerName: 'Config1CRC' },
        { id: 1, dbName: 'config2_name', headerName: 'Config2Name' },
        { id: 1, dbName: 'config2_crc', headerName: 'Config2CRC' },
        { id: 1, dbName: 'config3_name', headerName: 'Config3Name' },
        { id: 1, dbName: 'config3_crc', headerName: 'Config3CRC' },
        { id: 1, dbName: 'config4_name', headerName: 'Config4Name' },
        { id: 1, dbName: 'config4_crc', headerName: 'Config4CRC' },
        { id: 1, dbName: 'devuser_cfg_name', headerName: 'DevuserCfgName' },
        { id: 1, dbName: 'devuser_cfg_value', headerName: 'DevuserCfgValue' },
        { id: 1, dbName: 'product_code', headerName: 'ProductCode' }
    ];
    static readonly ALL_DEVICE_REPORT_COLUMN_FOR_EXPORT: Array<any> = [
        { id: 1, dbName: 'general.company_id', headerName: 'Customer' },
        { id: 1, dbName: 'report_header.device_id', headerName: 'DeviceID' },
        { id: 1, dbName: 'field_vehicle_vin.vin', headerName: 'VIN' },
        { id: 1, dbName: 'software_version.io', headerName: 'AssetID' },
        { id: 1, dbName: 'report_header.event_name', headerName: 'Event' },
        { id: 1, dbName: 'report_header.event_id', headerName: 'EventId' },
        { id: 1, dbName: 'general_mask_fields.received_time_stamp', headerName: 'ReceivedTime' },
        { id: 1, dbName: 'report_header.sequence', headerName: 'Seq' },
        { id: 1, dbName: 'report_header.ack_n', headerName: 'ACKn' },
        { id: 1, dbName: 'report_header.rtcdate_time_info_str', headerName: 'RTCTime' },
        { id: 1, dbName: 'general_mask_fields.gps_time', headerName: 'GPSTime' },
        { id: 1, dbName: 'general_mask_fields.gpsstatus_index', headerName: 'GPS' },
        { id: 1, dbName: 'general_mask_fields.latitude', headerName: 'Lat' },
        { id: 1, dbName: 'general_mask_fields.longitude', headerName: 'Long' },
        { id: 1, dbName: 'lat_long', headerName: 'LatLong' },
        { id: 1, dbName: 'general_mask_fields.external_power_volts', headerName: 'MainV' },
        { id: 1, dbName: 'voltage.aux_power', headerName: 'AltV' },
        { id: 1, dbName: 'general_mask_fields.internal_power_volts', headerName: 'BatrV' },
        { id: 1, dbName: 'voltage.charge_power', headerName: 'ChargingV' },
        { id: 1, dbName: 'general_mask_fields.device_status', headerName: 'DeviceStatus' },
        { id: 1, dbName: 'general_mask_fields.status', headerName: 'Power' },
        { id: 1, dbName: 'general_mask_fields.trip', headerName: 'InTrip' },
        { id: 1, dbName: 'general_mask_fields.ignition', headerName: 'Ignition' },
        { id: 1, dbName: 'general_mask_fields.motion', headerName: 'InMotion' },
        { id: 1, dbName: 'general_mask_fields.tamper', headerName: 'Tamper' },
        { id: 1, dbName: 'general_mask_fields.altitude_feet', headerName: 'Altitude' },
        { id: 1, dbName: 'general_mask_fields.speed_miles', headerName: 'MPH' },
        { id: 1, dbName: 'general_mask_fields.speed_kms', headerName: 'KPH' },
        { id: 1, dbName: 'general_mask_fields.heading', headerName: 'Heading' },
        { id: 1, dbName: 'general_mask_fields.odometer_kms', headerName: 'Odometer' },
        { id: 1, dbName: 'field_vehicle_ecu.fuel_level_percentage', headerName: 'Fuel' },
        { id: 1, dbName: 'field_vehicle_ecu.odtsince_milmiles', headerName: 'ODTMILMiles' },
        { id: 1, dbName: 'field_vehicle_ecu.odtsince_dtcclear_miles', headerName: 'ODTDTCClearMiles' },
        { id: 1, dbName: 'accelerometer_fields.accelerometer_info', headerName: 'AccInfo' },
        { id: 1, dbName: 'accelerometer_fields.accelerometer_xmm_s2', headerName: 'AccX' },
        { id: 1, dbName: 'accelerometer_fields.accelerometer_ymm_s2', headerName: 'AccY' },
        { id: 1, dbName: 'accelerometer_fields.accelerometer_zmm_s2', headerName: 'AccZ' },
        { id: 1, dbName: 'general_mask_fields.accelerometer_xdefault', headerName: 'AccCalibX' },
        { id: 1, dbName: 'general_mask_fields.accelerometer_ydefault', headerName: 'AccCalibY' },
        { id: 1, dbName: 'general_mask_fields.accelerometer_zdefault', headerName: 'AccCalibZ' },
        { id: 1, dbName: 'orientation_fields.orientation_status', headerName: 'Orientation' },
        { id: 1, dbName: 'orientation_fields.orientation_xaxis_milli_gs', headerName: 'OrientX' },
        { id: 1, dbName: 'orientation_fields.orientation_yaxis_milli_gs', headerName: 'OrientY' },
        { id: 1, dbName: 'orientation_fields.orientation_zaxis_milli_gs', headerName: 'OrientZ' },
        { id: 1, dbName: 'temperature.internal_temperature', headerName: 'IntTemp' },
        { id: 1, dbName: 'temperature.ambient_temperature', headerName: 'AmbientTemp' },
        { id: 1, dbName: 'general_mask_fields.rssi', headerName: 'RSSI' },
        { id: 1, dbName: 'general_mask_fields.hdop', headerName: 'HDOP' },
        { id: 1, dbName: 'general_mask_fields.num_satellites', headerName: 'Sats' },
        { id: 1, dbName: 'network_field.service_type_index', headerName: 'Service' },
        { id: 1, dbName: 'network_field.roaming_index', headerName: 'Roaming' },
        { id: 1, dbName: 'network_field.mobile_country_code', headerName: 'Country' },
        { id: 1, dbName: 'network_field.mobile_network_code', headerName: 'Network' },
        { id: 1, dbName: 'network_field.tower_id', headerName: 'TowerID' },
        { id: 1, dbName: 'network_field.tower_centroid_latitude', headerName: 'CentroidLat' },
        { id: 1, dbName: 'network_field.tower_centroid_longitude', headerName: 'CentroidLong' },
        { id: 1, dbName: 'network_field.cellular_band', headerName: 'Band' },
        { id: 1, dbName: 'network_field.rx_tx_ec', headerName: 'RxTxEc' },
        { id: 1, dbName: 'software_version.app_version', headerName: 'APPVer' },
        { id: 1, dbName: 'software_version.os_version', headerName: 'OSVer' },
        { id: 1, dbName: 'software_version.hw_id_version', headerName: 'HWVer' },
        { id: 1, dbName: 'software_version.hw_version_revision', headerName: 'HWRev' },
        { id: 1, dbName: 'software_version.hw_version_revision_1', headerName: 'IOVer' },
        { id: 1, dbName: 'software_version.extender_version', headerName: 'ExtenderVer' },
        { id: 1, dbName: 'software_version.ble_version', headerName: 'BLEVer' },
        { id: 1, dbName: 'config_version.device_config_changed', headerName: 'ConfigChg' },
        { id: 1, dbName: 'config_version.device_config', headerName: 'Config' },
        { id: 1, dbName: 'config_version.configuration_desc', headerName: 'ConfigDesc' },
        { id: 1, dbName: 'waterfall.config1_id', headerName: 'Devdefcfg' },
        { id: 1, dbName: 'waterfall.config1_crc', headerName: 'DevdefcfgCRC' },
        { id: 1, dbName: 'waterfall.config2_id', headerName: 'Devdef2cfg' },
        { id: 1, dbName: 'waterfall.config2_crc', headerName: 'Devdef2cfgCRC' },
        { id: 1, dbName: 'waterfall.config3_id', headerName: 'Devdef3cfg' },
        { id: 1, dbName: 'waterfall.config3_crc', headerName: 'Devdef3cfgCRC' },
        { id: 1, dbName: 'waterfall.config4_id', headerName: 'Devdef4cfg' },
        { id: 1, dbName: 'waterfall.config4_crc', headerName: 'Devdef4cfgCRC' },
        { id: 1, dbName: 'waterfall.config5_id', headerName: 'Devusrcfg' },
        { id: 1, dbName: 'waterfall.config5_crc', headerName: 'DevusrcfgCRC' },
        { id: 1, dbName: 'gpio.gpio_status', headerName: 'GPIO' },
        { id: 1, dbName: 'tftp_status.tftp_status', headerName: 'TFTPStatus' },
        { id: 1, dbName: 'peripheral_version', headerName: 'PeripheralVersion' },
        { id: 1, dbName: 'abs.status', headerName: 'ABS' },
        { id: 1, dbName: 'abs_odometer.odometer', headerName: 'ABSOdometer' },
        { id: 1, dbName: 'beta_abs_id.status', headerName: 'ABSInfo' },
        { id: 1, dbName: 'psi_air_supply.psi_air_supply_measure', headerName: 'AirSupply' },
        { id: 1, dbName: 'alpha_atis.condition', headerName: 'ATIS' },
        { id: 1, dbName: 'psi_wheel_end.psi_wheel_end_measure', headerName: 'WheelEnd' },
        { id: 1, dbName: 'skf_wheel_end.comm_status', headerName: 'SFKWheelEnd' },
        { id: 1, dbName: 'tpms_alpha.tpms_measures_all', headerName: 'AlphaTpms' },
        { id: 1, dbName: 'tpms_alpha.status', headerName: 'Receiver' },
        { id: 1, dbName: 'tpms_alpha.lof', headerName: 'LOF1' },
        { id: 1, dbName: 'tpms_alpha.lofTemp', headerName: 'LOF1Temp' },
        { id: 1, dbName: 'tpms_alpha.lofPsi', headerName: 'LOF1Psi' },
        { id: 1, dbName: 'tpms_alpha.lif', headerName: 'LIF2' },
        { id: 1, dbName: 'tpms_alpha.lifTemp', headerName: 'LIF2Temp' },
        { id: 1, dbName: 'tpms_alpha.lifPsi', headerName: 'LIF2Psi' },
        { id: 1, dbName: 'tpms_alpha.rif', headerName: 'RIF3' },
        { id: 1, dbName: 'tpms_alpha.rifTemp', headerName: 'RIF3Temp' },
        { id: 1, dbName: 'tpms_alpha.rifPsi', headerName: 'RIF3Psi' },
        { id: 1, dbName: 'tpms_alpha.rof', headerName: 'ROF4' },
        { id: 1, dbName: 'tpms_alpha.rofTemp', headerName: 'ROF4Temp' },
        { id: 1, dbName: 'tpms_alpha.rofPsi', headerName: 'ROF4Psi' },
        { id: 1, dbName: 'tpms_alpha.lor', headerName: 'LOR5' },
        { id: 1, dbName: 'tpms_alpha.lorTemp', headerName: 'LOR5Temp' },
        { id: 1, dbName: 'tpms_alpha.lorPsi', headerName: 'LOR5Psi' },
        { id: 1, dbName: 'tpms_alpha.lir', headerName: 'LIR6' },
        { id: 1, dbName: 'tpms_alpha.lirTemp', headerName: 'LIR6Temp' },
        { id: 1, dbName: 'tpms_alpha.lirPsi', headerName: 'LIR6Psi' },
        { id: 1, dbName: 'tpms_alpha.rir', headerName: 'RIR7' },
        { id: 1, dbName: 'tpms_alpha.rirTemp', headerName: 'RIR7Temp' },
        { id: 1, dbName: 'tpms_alpha.rirPsi', headerName: 'RIR7Psi' },
        { id: 1, dbName: 'tpms_alpha.ror', headerName: 'ROR8' },
        { id: 1, dbName: 'tpms_alpha.rorTemp', headerName: 'ROR8Temp' },
        { id: 1, dbName: 'tpms_alpha.rorPsi', headerName: 'ROR8Psi' },
        { id: 1, dbName: 'tpms_beta.allValues', headerName: 'BetaTPMS' },
        { id: 1, dbName: 'tpms_beta.status', headerName: 'Receivers' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lof', headerName: 'LOF' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lof_temp', headerName: 'LOF1TempBeta' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lof_psi', headerName: 'LOF1PsiBeta' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lif', headerName: 'LIF' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lif_temp', headerName: 'LIF1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lif_psi', headerName: 'LIF1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rif', headerName: 'RIF' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rif_temp', headerName: 'RIF1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rif_psi', headerName: 'RIF1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rof', headerName: 'ROF' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rof_temp', headerName: 'ROF1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rof_psi', headerName: 'ROF1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lor', headerName: 'LOR' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lor_temp', headerName: 'LOR1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lor_psi', headerName: 'LOR1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lir', headerName: 'LIR' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lir_temp', headerName: 'LIR1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.lir_psi', headerName: 'LIR1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rir', headerName: 'RIR' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rir_temp', headerName: 'RIR1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.rir_psi', headerName: 'RIR1Psi' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.ror', headerName: 'ROR' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.ror_temp', headerName: 'ROR1Temp' },
        { id: 1, dbName: 'tpms_beta.tpms_beta_measure.ror_psi', headerName: 'ROR1Psi' },
        { id: 1, dbName: 'beacon.comm_status', headerName: 'BeaconSmartPair' },
        { id: 1, dbName: 'brakestroke', headerName: 'BrakeStroke' },
        { id: 1, dbName: 'chassis', headerName: 'ChassisCargo' },
        { id: 1, dbName: 'cargo_camera_sensor.uri', headerName: 'Thumbnail' },
        { id: 1, dbName: 'cargo_camera_sensor.state', headerName: 'State' },
        { id: 1, dbName: 'cargo_camera_sensor.prediction_value', headerName: 'Loaded' },
        { id: 1, dbName: 'cargo_camera_sensor.confidence_rating', headerName: 'ConfidenceRating' },
        { id: 1, dbName: 'door_sensor.status', headerName: 'Door' },
        { id: 1, dbName: 'ble_door_sensor.comm_status', headerName: 'DoorWireless' },
        { id: 1, dbName: 'advertisement_maxlink.lift_gate_voltage', headerName: 'LiftgateVoltage' },
        { id: 1, dbName: 'advertisement_maxlink.state_of_charge', headerName: 'StateOfCharge' },
        { id: 1, dbName: 'connectable_maxlink.lift_gate_cycles', headerName: 'LiftGateCycles' },
        { id: 1, dbName: 'connectable_maxlink.motor1_runtime', headerName: 'Motor1Runtime' },
        { id: 1, dbName: 'connectable_maxlink.motor2_runtime', headerName: 'Motor2Runtime' },
        { id: 1, dbName: 'connectable_maxlink.status_flags', headerName: 'StatusFlags' },
        { id: 1, dbName: 'lite_sentry', headerName: 'LightOut' },
        { id: 1, dbName: 'reefer.concatValues', headerName: 'Reefer' },
        { id: 1, dbName: 'reefer.status', headerName: 'Status' },
        { id: 1, dbName: 'reefer.state', headerName: 'TempControl' },
        { id: 1, dbName: 'reefer.fan_state', headerName: 'AirFlow' },
        { id: 1, dbName: 'tank_saver_beta.concatValues', headerName: 'TankSaverBeta' },
        { id: 1, dbName: 'pepsi_temperature.all_status', headerName: 'TempMinew' },
        { id: 1, dbName: 'pepsi_temperature.comm_status', headerName: 'CommStatus' },
        { id: 1, dbName: 'pepsi_temperature.sensor_id', headerName: 'SensorId' },
        { id: 1, dbName: 'pepsi_temperature.s_status', headerName: 'SensorStatus' },
        { id: 1, dbName: 'pepsi_temperature.age_of_reading', headerName: 'AgeOfReading' },
        { id: 1, dbName: 'pepsi_temperature.battery_level', headerName: 'BatteryLevel' },
        { id: 1, dbName: 'pepsi_temperature.temperature', headerName: 'Temperature' },
        { id: 1, dbName: 'pepsi_temperature.humidity', headerName: 'Humidity' },
        { id: 1, dbName: 'ble_temperature.comm_status', headerName: 'TempWirelessStatus' },
        { id: 1, dbName: 'ble_temperature.ble_temperature_sensormeasure', headerName: 'TempWirelessReadings' },
        { id: 1, dbName: 'general.device_ip', headerName: 'DeviceIP' },
        { id: 1, dbName: 'general.rawreport', headerName: 'RawReport' }
    ];

    static readonly LANDING_PAGE_LIST: Array<any> = [
        { id: 1, name: 'Device List', value: 'DEVICE_LIST', url: 'gateway-list' },
        { id: 2, name: 'All Device Reports', value: 'ALL_DEVICE_REPORT', url: 'all-report-list' },
        // { id: 3, name: 'Devices By Customer', value: 'DEVICE_BY_CUSTOMER', url: 'gatewaysummary' },
        { id: 4, name: 'All Packages', value: 'ALL_PACKAGES', url: 'packages' },
        { id: 5, name: 'All Campaigns', value: 'ALL_CAMPAIGNS', url: 'campaigns' },
        { id: 6, name: 'Installation Report', value: 'INSTALLATION_REPORT', url: 'installation-summary' },
    ];

    static readonly ASSET_CATEGORY: Array<any> = [
        { id: 1, value: 'TRAILER', data: 'TRAILER', gateway: 'Smart7' },
        { id: 2, value: 'CHASSIS', data: 'CHASSIS', gateway: 'ChassisNet' },
        { id: 3, value: 'CONTAINER', data: 'CONTAINER', gateway: 'ContainerNet' },
        { id: 4, value: 'VEHICLE', data: 'VEHICLE', gateway: 'DriveTrac' },
        { id: 5, value: 'TRACTOR', data: 'TRACTOR', gateway: 'AssetTrac' }
    ];
    static readonly HARDWARE_TYPE: Array<any> = [
        { id: 1, value: '4015R00', data: 'Arrow-L P1.1.4 with Freight P1.0.0' },
        { id: 2, value: '4015R01', data: 'Arrow-L P1.1.4 with Freight P2.0.0' },
        { id: 3, value: '4015R02', data: 'Arrow-L P1.1.4 with Freight P2.0.2' },
        { id: 4, value: '4015R03', data: 'Arrow-L P1.1.4 with Freight P2.0.3/2.0.5' },
        { id: 5, value: '4115R10', data: 'Arrow-LA P2.0.1 with Freight P5.1.0' },
    ];

    static readonly PRODUCT_CODE: Array<any> = [
        { id: 1, data: '77-6290', value: '77-6290' },
        { id: 2, data: '77-6320', value: '77-6320' },
        { id: 3, data: '77-6800', value: '77-6800' },
        { id: 4, data: '77-6950', value: '77-6950' },
        { id: 5, data: '77-6900', value: '77-6900' },
        { id: 6, data: '77-6990', value: '77-6990' },
        { id: 7, data: '77-6270', value: '77-6270' },
        { id: 8, data: '77-6220', value: '77-6220' },
        { id: 9, data: '77-6910', value: '77-6910' },
        { id: 10, data: '77-6960', value: '77-6960' },
        { id: 11, data: '77-6400', value: '77-6400' },
        { id: 12, data: '77-6700', value: '77-6700' },
        { id: 13, data: '77-6500', value: '77-6500' },
        { id: 14, data: '77-6280', value: '77-6280' },
        { id: 15, data: '77-6298', value: '77-6298' },
        { id: 16, data: '55-1110', value: '55-1110' },
        { id: 17, data: '77-6420', value: '77-6420' },
        { id: 18, data: '77-6920', value: '77-6920' },
        { id: 19, data: '77-6930', value: '77-6930' },
        { id: 20, data: '77-7110', value: '77-7110' },
        { id: 21, data: '77-6530', value: '77-6530' },
        { id: 22, data: '77-6970', value: '77-6970' },
        { id: 23, data: '77-6230', value: '77-6230' },
        { id: 24, data: '77-6940', value: '77-6940' },
        { id: 25, data: '77-6830', value: '77-6830' },
        { id: 26, data: '77-6600', value: '77-6600' },
        { id: 27, data: '77-7310', value: '77-7310' },
        { id: 28, data: '77-6401', value: '77-6401' },
        { id: 29, data: '77-6250', value: '77-6250' },
        { id: 30, data: '77-7100', value: '77-7100' },
        { id: 31, data: '77-7120', value: '77-7120' },
        { id: 32, data: '77-6330', value: '77-6330' },
        { id: 33, data: '77-6240', value: '77-6240' },
        { id: 34, data: '77-6720', value: '77-6720' },
        { id: 35, data: '6600-77', value: '6600-77' },
        { id: 36, data: '77-6770', value: '77-6770' },
        { id: 37, data: '77-6430', value: '77-6430' },
        { id: 38, data: '77-7300', value: '77-7300' },
        { id: 39, data: '77-6790', value: '77-6790' },
        { id: 40, data: '77-6980', value: '77-6980' },
        { id: 41, data: '77-6210', value: '77-6210' },
        { id: 42, data: '77-680', value: '77-680' },
        { id: 43, data: '77-6290 gt-att-cn', value: '77-6290 gt-att-cn' },
        { id: 44, data: '77-6292', value: '77-6292' },
        { id: 45, data: '23', value: '23' },
        { id: 46, data: '77-H101', value: '77-H101' },
        { id: 47, data: '77-S107', value: '77-S107' },
        { id: 48, data: '77-S180', value: '77-S180' },
        { id: 49, data: '77-S108', value: '77-S108' },
        { id: 50, data: '77-S188', value: '77-S188' },
        { id: 51, data: '77-S206', value: '77-S206' },
        { id: 52, data: '77-S202', value: '77-S202' },
        { id: 53, data: '77-S203', value: '77-S203' },
        { id: 54, data: '77-S177', value: '77-S177' },
        { id: 55, data: '77-S111', value: '77-S111' },
        { id: 56, data: '77-6520', value: '77-6520' },
        { id: 57, data: '77-6710', value: '77-6710' },
        { id: 58, data: '998', value: '998' },
        { id: 59, data: '77-6710 cc1', value: '77-6710 cc1' },
        { id: 60, data: '77-S500', value: '77-S500' },
        { id: 61, data: '77-S102', value: '77-S102' },
        { id: 62, data: '77-S119', value: '77-S119' },
        { id: 63, data: '77-S137', value: '77-S137' },
        { id: 64, data: '77-S164', value: '77-S164' },
        { id: 65, data: '77-S120', value: '77-S120' },
        { id: 66, data: '77-S187', value: '77-S187' },
        { id: 67, data: '77-DB40', value: '77-DB40' },
        { id: 68, data: '77-S191', value: '77-S191' },
        { id: 69, data: '77-S239', value: '77-S239' },
        { id: 70, data: '77-6830 kt1', value: '77-6830 kt1' }
    ];

    static readonly COLOR_CODE: Array<any> = [
        '#FF0000', '#A52A2A', '#F1C40F', '#17202A', '#5B2C6F', '#6E2C00', '#1B4F72', '#BA4A00', '#FFC0CB', '#FF00FF',
        '#7FFFD4', '#808000', '#008000', '#800000', '#A52A2A', '#FFA500', '#000000', '#808080', '#C0C0C0', '#FFFFFF',
        '#98AFC7', '#728FCE', '#4863A0', '#151B54', '#95B9C7', '#16E2F5', '#7FFFD4', '#008B8B', '#033E3E', '#31906E',
        '#728C00', '#808000', '#4B5320', '#006400', '#254117', '#FFFFC2', '#F7E7CE', '#F2BB66', '#FFA500', '#483C32',
        '#EB5406', '#9F000F', '#2B1B17', '#FDD7E4', '#F778A1', '#CCCCFF', '#967BB6', '#86608E', '#4E5180',
    ];

    static readonly DOOR_TYPE: Array<any> = [
        { id: 1, value: 'ROLL', data: 'ROLL' },
        { id: 2, value: 'SWING', data: 'SWING' },
        { id: 3, value: 'SIDE', data: 'SIDE' },
        { id: 4, value: 'OTHER', data: 'OTHER' },
    ];

    static readonly ASSOCIATION_CUSTOMER_LIST: Array<any> = ['Nussbaum', 'Werner Enterprises',
        'Hirschbach Motor Lines', 'Globe Logistics Inc', 'Ryder'
    ];
}
