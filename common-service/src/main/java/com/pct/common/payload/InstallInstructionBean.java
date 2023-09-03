package com.pct.common.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 17/06/20
 */

@Data
@NoArgsConstructor
public class InstallInstructionBean {

    private String instruction;
    private int sequence;
}
