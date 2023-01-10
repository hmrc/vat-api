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
            <td><p>Default (no header value)</p></td>
            <td><p>Simulates the scenario where the endpoint has indicated that no associated data could be found.</p></td>
        </tr>
        <tr>
            <td><p>SINGLE_LIABILITY</p></td>
            <td><p>Returns a single valid liability when used with dates from 2017-01-02 and to 2017-02-02.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_LIABILITIES</p></td>
            <td><p>Returns multiple valid liabilities when used with dates from 2017-04-05 and to 2017-12-21.</p></td>
        </tr>
        <tr>
            <td><p>SINGLE_LIABILITY_2018_19</p></td>
            <td><p>Returns a single valid liability when used with dates from 2018-01-02 and to 2018-02-02.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_LIABILITIES_2018_19</p></td>
            <td><p>Returns multiple valid liabilities when used with dates from 2018-04-05 and to 2018-12-21.</p></td>
        </tr>
        <tr>
            <td><p>INSOLVENT_TRADER</p></td>
            <td><p>Simulates the scenario where the client is an insolvent trader.</p></td>
        </tr>
    </tbody>
</table>
<p>The 'to' date of the liability must fall within the date range provided.</p>
