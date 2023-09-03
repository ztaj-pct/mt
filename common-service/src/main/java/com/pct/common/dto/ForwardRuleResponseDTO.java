package com.pct.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties
public class ForwardRuleResponseDTO {

	private String imei;

	private List<DeviceFwdRulResp> deviceFwdRulResps;

	private boolean status;

	public class DeviceFwdRulResp {
		private String forwardingRuleUrlUuid;
		private String message;
		private boolean status;

		public String getForwardingRuleUrlUuid() {
			return forwardingRuleUrlUuid;
		}

		public void setForwardingRuleUrlUuid(String forwardingRuleUrlUuid) {
			this.forwardingRuleUrlUuid = forwardingRuleUrlUuid;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

	}

}
