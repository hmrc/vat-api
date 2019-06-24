<p>Scenario simulations using Gov-Test-Scenario headers are only available in sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>Default (No header value)</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and one is fulfilled</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_NONE_MET</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and none are fulfilled</p></td>
        </tr>
        <tr>
        <tr>
            <td><p>QUARTERLY_ONE_MET</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and one is fulfilled</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_TWO_MET</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and two are fulfilled</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_THREE_MET</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and three are fulfilled</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_FOUR_MET</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations and four are fulfilled</p></td>
        </tr>        
        <tr>
            <td><p>MONTHLY_NONE_MET</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations and none are fulfilled</p></td>
        </tr>
        <tr>
            <td><p>MONTHLY_ONE_MET</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations and one month is fulfilled</p></td>
        </tr>
        <tr>
            <td><p>MONTHLY_TWO_MET</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations and two months are fulfilled</p></td>
        </tr>
        <tr>
            <td><p>MONTHLY_THREE_MET</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations and three months are fulfilled</p></td>
        </tr>
        <tr>
            <td><p>MONTHLY_OBS_##_OPEN</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations for 2018 and the ## month is open. All previous obligations for same year will show as fulfilled. Accepts 01 to 12. 
                   Example: MONTHLY_OBS_03_OPEN</p></td>
        </tr>
        <tr>
            <td><p>MONTHLY_OBS_12_FULFILLED</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations for 2018 and all obligations are fulfilled.</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_OBS_##_OPEN</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations for 2018 and the ## quarter is open.  All previous obligations for same year will show as fulfilled. Accepts 01 to 04. 
                   Example: QUARTERLY_OBS_02_OPEN</p></td>
        </tr>
        <tr>
            <td><p>QUARTERLY_OBS_04_FULFILLED</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations for 2018 and all obligations are fulfilled.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_OPEN_MONTHLY</p></td>
            <td><p>Simulates the scenario where the client has monthly obligations for 2018 and two are open.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_OPEN_QUARTERLY</p></td>
            <td><p>Simulates the scenario where the client has quarterly obligations for 2018 and two are open.</p></td>
        </tr>
        <tr>
            <td><p>OBS_SPANS_MULTIPLE_YEARS</p></td>
            <td><p>Simulates the scenario where the client has an obligation that spans both calendar years 2018 and 2019.</p></td>
        </tr>
        <tr>
            <td><p>NOT_FOUND</p></td>
            <td><p>Simulates the scenario where no data is found</p></td>
        </tr>                  
    </tbody>
</table>
