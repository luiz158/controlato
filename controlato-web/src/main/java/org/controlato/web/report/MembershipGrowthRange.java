/* Controlato is a web-based service conceived and designed to assist families
 * on the control of their daily lives.
 * Copyright (C) 2012 Controlato.org
 *
 * Controlato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Controlato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with Controlato. Look for the file LICENSE.txt at the root level.
 * If you do not, see http://www.gnu.org/licenses/.
 * */
package org.controlato.web.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.controlato.entity.UserAccount;
import org.controlato.web.util.ResourceBundle;

/**
 * This class feeds the line chart that shows the growth of the user group.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class MembershipGrowthRange {

    private String rangeName;
    private Integer year;
    private Integer month;
    private Integer value;
    private Integer acumulated;

    public MembershipGrowthRange() {}

    private MembershipGrowthRange(String rangeName, Integer year, Integer month, Integer value, Integer acumulated) {
        this.rangeName = rangeName;
        this.year = year;
        this.month = month;
        this.value = value;
        this.acumulated = acumulated;
    }

    public static List<MembershipGrowthRange> generateSeries(List<UserAccount> userAccounts) {
        List<MembershipGrowthRange> membershipGrowthRanges = new ArrayList<>();

        ResourceBundle bundle = ResourceBundle.getInstance();
        String[] months = {bundle.getMessage("januaryShort"),
                           bundle.getMessage("februaryShort"),
                           bundle.getMessage("marchShort"),
                           bundle.getMessage("aprilShort"),
                           bundle.getMessage("mayShort"),
                           bundle.getMessage("juneShort"),
                           bundle.getMessage("julyShort"),
                           bundle.getMessage("augustShort"),
                           bundle.getMessage("septemberShort"),
                           bundle.getMessage("octoberShort"),
                           bundle.getMessage("novemberShort"),
                           bundle.getMessage("decemberShort")};

        Date registrationDate;
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        MembershipGrowthRange membershipGrowthRange = null;
        int year = 0, month = 0, acumulated = 0;
        boolean incremented = false;
        for(UserAccount userAccount: userAccounts) {
            registrationDate = userAccount.getRegistrationDate();
            date.setTime(registrationDate);

            if(year != date.get(Calendar.YEAR)) {
                year = date.get(Calendar.YEAR);
                incremented = true;
            }

            if(month != date.get(Calendar.MONTH)) {
                month = date.get(Calendar.MONTH);
                incremented = true;
            }

            if(incremented) {
                membershipGrowthRange = new MembershipGrowthRange(months[month] + ", " + year, year, month, 1, ++acumulated);
                membershipGrowthRanges.add(membershipGrowthRange);
                incremented = false;
            }
            else {
                acumulated += membershipGrowthRange.incrementValue();
            }
        }

        Date deactivationDate;
        for(UserAccount userAccount: userAccounts) {
            if(!userAccount.getDeactivated()) {
                continue;
            }

            deactivationDate = userAccount.getDeactivationDate();

            date.setTime(deactivationDate);
            year = date.get(Calendar.YEAR);
            month = date.get(Calendar.MONTH);

            for(MembershipGrowthRange mgr: membershipGrowthRanges) {
                if(mgr.isSamePeriod(year, month)) {
                    mgr.decrementValue();
                }
            }
        }

        return membershipGrowthRanges;
    }

    public String getRangeName() {
        return rangeName;
    }

    public void setRangeName(String rangeName) {
        this.rangeName = rangeName;
    }

    public boolean isSamePeriod(Integer year, Integer month) {
        if(this.year.equals(year) && this.month.equals(month)) {
            return true;
        }
        else {
            return false;
        }
    }

    public Integer getValue() {
        return value;
    }

    public Integer incrementValue() {
        this.value++;
        this.acumulated++;
        return 1;
    }

    public void decrementValue() {
        this.value--;
        this.acumulated--;
    }

    public Integer getAcumulated() {
        return acumulated;
    }

    public void setAcumulated(Integer acumulated) {
        this.acumulated = acumulated;
    }
}