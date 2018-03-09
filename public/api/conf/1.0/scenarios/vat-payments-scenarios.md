<p>Scenario simulations using <b> Gov-Test-Scenario </b> headers is only available in sandbox environment</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>SINGLE_PAYMENT</p></td>
            <td><p>Returns a single valid payment when used with dates from 2017-01-02 and to 2017-02-02.</p></td>
        </tr>
        <tr>
            <td><p>MULTIPLE_PAYMENTS</p></td>
            <td><p>Returns multiple valid payments when used with dates from 2017-02-27 and to 2017-12-21.</p></td>
        </tr>
        <tr>
            <td><p>CLIENT_NOT_SUBSCRIBED</p></td>
            <td><p>Simulate client subscription to MTD VAT check failure</p></td>
        </tr>
    </tbody>
</table>
<p>The 'to' date of the payment must fall within the date range provided.</p>