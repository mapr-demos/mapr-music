import {Component, OnInit} from "@angular/core";
import {ReportingService} from "../../services/reporting.service";
import {ActivatedRoute, Router} from "@angular/router";
import "rxjs/add/operator/switchMap";

@Component({
  selector: 'reporting-page',
  templateUrl: './reporting-page.component.html'
})
export class ReportingPage implements OnInit{
  reportingOptions = [
    {
      value: 'TOP_5',
      label: 'Top 5 languages'
    },
    {
      value: 'LAST_10_YEAR',
      label: 'Last 10 albums by year'
    },
    {
      value: 'TOP_10_AREA',
      label: 'Top 10 area'
    }
  ];
  selectedReportOption = null;
  reportingResults = null;
  sourceURL = null;
  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private reportingService: ReportingService
  ) {
  }
  ngOnInit(): void {
    this.activatedRoute.queryParams
      .switchMap(({report = 'TOP_5'}) => {
        this.selectedReportOption = report;
        this.sourceURL = this.reportingService.getReportsURL(report);
        return this.reportingService.getReports(report)
      })
      .subscribe((reportResults) => {
        this.reportingResults = reportResults;
      });
  }
  onSelectReportOption(event, optionValue) {
    event.preventDefault();
    const queryParams = {
      report: optionValue
    };
    this.router.navigate(['reporting'], {queryParams});
  }
}
